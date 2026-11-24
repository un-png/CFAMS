package com.cfams.repair.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.cfams.campus.service.CampusAccessService;
import com.cfams.asset.service.CampusAssetStateService;
import com.cfams.asset.domain.vo.AssetStateVO;
import com.cfams.common.exception.ServiceException;
import com.cfams.common.utils.SecurityUtils;
import com.cfams.message.service.CampusMessageService;
import com.cfams.repair.domain.dto.RepairQueryDTO;
import com.cfams.repair.domain.vo.RepairRecordVO;
import com.cfams.repair.domain.vo.RepairVO;
import com.cfams.repair.mapper.CampusRepairMapper;
import com.cfams.repair.service.CampusRepairService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/** 业务服务实现，保留原有业务校验及状态处理规则。 */
@Service
public class CampusRepairServiceImpl implements CampusRepairService {

    @Autowired
    private CampusRepairMapper campusRepairMapper;

    @Autowired
    private CampusAccessService campusAccessService;

    @Autowired
    private CampusAssetStateService campusAssetStateService;

    @Autowired
    private CampusMessageService campusMessageService;

    /**
     * 填充当前用户可访问的数据范围。
     *
     * @param query 查询条件及数据范围
     */
    @Override
    public void scope(RepairQueryDTO query) {
        campusAccessService.scope(query);
    }

    /**
     * 按查询条件读取报修单列表。
     *
     * @param query 查询条件及数据范围
     * @return 符合条件的报修单列表
     */
    @Override
    public List<RepairVO> list(RepairQueryDTO query) {
        return campusRepairMapper.list(query);
    }

    /**
     * 查询报修单详情并校验当前用户的数据访问权限。
     *
     * @param id 报修单编号
     * @return 已通过权限或存在性校验的报修单
     */
    @Override
    public RepairVO detail(Long id) {
        RepairVO r = exists(campusRepairMapper.find(id));
        Long owner =
                Objects.equals(r.repairerUserId, SecurityUtils.getUserId())
                        ? r.repairerUserId
                        : r.reporterUserId;
        campusAccessService.requireData(r.assetDeptId, owner);
        return r;
    }

    /**
     * 查询维修过程记录，读取前检查维修单访问权限。
     *
     * @param id 报修单编号
     * @return 按操作时间排序的维修过程记录列表
     */
    @Override
    public List<RepairRecordVO> records(Long id) {
        detail(id);
        return campusRepairMapper.records(id);
    }

    /**
     * 校验权限、任务状态和维修执行资格后受理、驳回或改派，联动资产及通知。
     *
     * @param id 报修单编号
     * @param accept 是否受理
     * @param repairer 维修人员
     * @param opinion 处理意见
     * @param reassign 是否为改派操作
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    @Override
    public void assign(Long id, boolean accept, Long repairer, String opinion, boolean reassign) {
        campusAccessService.requirePermission("campus:repairs:assign");
        RepairVO initial = exists(campusRepairMapper.find(id));
        AssetStateVO asset = campusAssetStateService.selectForChange(initial.assetId);
        RepairVO r = exists(campusRepairMapper.selectForChange(id));
        campusAccessService.requireDepartment(r.assetDeptId);
        campusAccessService.requireDepartment(asset.getDeptId());
        check(!"3".equals(asset.getAssetStatus()), "报废资产不能办理维修");
        if (reassign) {
            check(accept && ("1".equals(r.status) || "2".equals(r.status)), "仅已派工或维修中任务可改派");
            required(opinion);
            check(!Objects.equals(repairer, r.repairerUserId), "新维修人与当前人员相同");
        } else {
            check("0".equals(r.status), "仅待受理报修可以受理或驳回");
        }
        if (accept) {
            campusAccessService.requireUser(repairer);
            check(campusRepairMapper.qualifiedRepairer(repairer) > 0, "指定人员未获维修执行权限 campus:repair:execute");
        } else {
            required(opinion);
        }
        String before = campusAssetStateService.snapshot(r.assetId);
        String next = accept ? "1" : "5";
        changed(
                campusRepairMapper.assign(
                        id,
                        r.status,
                        next,
                        repairer,
                        opinion,
                        SecurityUtils.getUserId(),
                        SecurityUtils.getUsername()));
        String content = opinion == null || opinion.isBlank() ? "受理并派工" : opinion;
        if (accept && !reassign) {
            record(r, "ACCEPT", r.status, r.status, null, content);
        }
        RepairRecordVO event =
                record(
                        r,
                        accept ? "ASSIGN" : "REJECT",
                        r.status,
                        next,
                        accept ? repairer : null,
                        content);
        if (accept) {
            boolean borrowed = campusRepairMapper.activeBorrow(r.assetId) > 0;
            campusAssetStateService.changeState(r.assetId, asset.getAssetStatus(), borrowed ? "1" : "2", "1");
            campusMessageService.notify(
                    repairer,
                    "REPAIR:ASSIGN:" + event.recordId,
                    "REPAIR",
                    "REPAIR",
                    id,
                    "维修任务指派",
                    content);
            if (reassign && r.repairerUserId != null) {
                campusMessageService.notify(
                        r.repairerUserId,
                        "REPAIR:REASSIGNED:" + event.recordId,
                        "REPAIR",
                        "REPAIR",
                        id,
                        "维修任务已改派",
                        content);
            }
        } else if ("2".equals(asset.getAssetStatus()) && campusRepairMapper.otherRepairs(r.assetId, id) == 0) {
            campusAssetStateService.changeState(
                    r.assetId,
                    asset.getAssetStatus(),
                    campusRepairMapper.activeBorrow(r.assetId) > 0 ? "1" : "0",
                    asset.getConditionStatus());
        }
        campusAssetStateService.history(r.assetId, "REPAIR", "REPAIR", id, before, content);
        campusMessageService.notify(
                r.reporterUserId,
                "REPAIR:HANDLED:" + event.recordId,
                "REPAIR",
                "REPAIR",
                id,
                "报修处理结果",
                content);
    }

    /**
     * 核验维修反馈并联动资产状态、过程记录及相关人员通知。
     *
     * @param id 报修单编号
     * @param approve 是否通过
     * @param opinion 处理意见
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    @Override
    public void verify(Long id, boolean approve, String opinion) {
        campusAccessService.requirePermission("campus:repairVerify:verify");
        RepairVO initial = exists(campusRepairMapper.find(id));
        AssetStateVO asset = campusAssetStateService.selectForChange(initial.assetId);
        RepairVO r = exists(campusRepairMapper.selectForChange(id));
        campusAccessService.requireDepartment(r.assetDeptId);
        campusAccessService.requireDepartment(asset.getDeptId());
        check("3".equals(r.status), "仅待核验维修单可以核验");
        check(!"3".equals(asset.getAssetStatus()), "报废资产不能核验维修");
        check(!Objects.equals(r.repairerUserId, SecurityUtils.getUserId()), "维修执行人不能核验自己的结果");
        if (!approve) {
            required(opinion);
        } else {
            check("1".equals(r.repairResult) || "2".equals(r.repairResult), "维修反馈结果不完整");
            check(r.repairCost != null && r.repairCost.signum() >= 0, "维修反馈费用无效");
            check(r.resultDescription != null && !r.resultDescription.isBlank(), "缺少维修结果说明");
        }
        String before = campusAssetStateService.snapshot(r.assetId);
        String next = approve ? "4" : "2";
        changed(
                campusRepairMapper.verify(
                        id, next, opinion, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
        String content = opinion == null || opinion.isBlank() ? "维修结果核验通过" : opinion;
        RepairRecordVO event =
                record(r, approve ? "VERIFY" : "RETURN", r.status, next, r.repairerUserId, content);
        boolean borrowed = campusRepairMapper.activeBorrow(r.assetId) > 0;
        boolean repairing = !approve || campusRepairMapper.otherRepairs(r.assetId, id) > 0;
        String condition = approve && !repairing && "1".equals(r.repairResult) ? "0" : "1";
        campusAssetStateService.changeState(
                r.assetId,
                asset.getAssetStatus(),
                borrowed ? "1" : repairing ? "2" : "0",
                condition);
        campusAssetStateService.history(r.assetId, "REPAIR", "REPAIR", id, before, content);
        campusMessageService.notify(
                r.reporterUserId,
                "REPAIR:VERIFY:REPORTER:" + event.recordId,
                "REPAIR",
                "REPAIR",
                id,
                "维修核验结果",
                content);
        if (r.repairerUserId != null) {
            campusMessageService.notify(
                    r.repairerUserId,
                    "REPAIR:VERIFY:EXECUTOR:" + event.recordId,
                    "REPAIR",
                    "REPAIR",
                    id,
                    "维修核验结果",
                    content);
        }
    }

    /**
     * 保存包含操作人、状态流转和费用结果快照的维修过程记录。
     *
     * @param r 维修单
     * @param action 业务操作类型
     * @param from 变更前状态
     * @param to 变更后状态
     * @param target 目标维修人员编号
     * @param content 业务说明或消息正文
     * @return 已通过权限或存在性校验的报修单
     */
    private RepairRecordVO record(
            RepairVO r, String action, String from, String to, Long target, String content) {
        RepairRecordVO event = new RepairRecordVO();
        event.repairId = r.repairId;
        event.actionType = action;
        event.fromStatus = from;
        event.toStatus = to;
        event.operatorUserId = SecurityUtils.getUserId();
        event.targetUserId = target;
        event.content = content;
        event.costSnapshot = r.repairCost;
        event.resultSnapshot = r.repairResult;
        changed(campusRepairMapper.record(event));
        return event;
    }

    /**
     * 校验报修单存在并返回该单据。
     *
     * @param r 维修单
     * @return 已通过权限或存在性校验的报修单
     */
    private static RepairVO exists(RepairVO r) {
        check(r != null, "维修单不存在");
        return r;
    }

    /**
     * 校验处理意见非空且不全为空白。
     *
     * @param value 待校验的处理意见
     */
    private static void required(String value) {
        check(value != null && !value.isBlank(), "处理意见不能为空");
    }

    /**
     * 要求状态条件更新恰好影响一行，防止重复处理。
     *
     * @param rows 状态更新影响行数
     */
    private static void changed(int rows) {
        check(rows == 1, "单据状态已变化，请刷新");
    }

    /**
     * 业务条件不满足时抛出业务异常。
     *
     * @param valid 业务条件是否满足
     * @param message 校验失败提示
     */
    private static void check(boolean valid, String message) {
        if (!valid) {
            throw new ServiceException(message);
        }
    }
}
