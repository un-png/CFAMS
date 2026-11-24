package com.cfams.borrow.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.cfams.borrow.domain.dto.BorrowQueryDTO;
import com.cfams.borrow.domain.vo.BorrowVO;
import com.cfams.borrow.domain.vo.HandoverVO;
import com.cfams.borrow.mapper.CampusBorrowMapper;
import com.cfams.borrow.service.CampusBorrowService;
import com.cfams.campus.service.CampusAccessService;
import com.cfams.asset.service.CampusAssetStateService;
import com.cfams.asset.domain.vo.AssetStateVO;
import com.cfams.common.exception.ServiceException;
import com.cfams.common.utils.SecurityUtils;
import com.cfams.message.service.CampusMessageService;
import com.cfams.system.service.ISysConfigService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** 业务服务实现，保留原有业务校验及状态处理规则。 */
@Service
public class CampusBorrowServiceImpl implements CampusBorrowService {

    @Autowired
    private CampusBorrowMapper campusBorrowMapper;

    @Autowired
    private CampusAccessService campusAccessService;

    @Autowired
    private CampusAssetStateService campusAssetStateService;

    @Autowired
    private CampusMessageService campusMessageService;

    private TransactionTemplate reminderTransaction;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private ISysConfigService iSysConfigService;

    /** 依赖注入完成后初始化临期提醒事务，保持独立事务和读已提交隔离级别。 */
    @jakarta.annotation.PostConstruct
    public void initializeReminderTransaction() {
        reminderTransaction = new TransactionTemplate(platformTransactionManager);
        reminderTransaction.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        reminderTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * 填充当前用户数据范围及业务查询配置。
     *
     * @param query 查询条件及数据范围
     */
    @Override
    public void scope(BorrowQueryDTO query) {
        campusAccessService.scope(query);
        query.setRemindHours(remindHours());
    }

    /**
     * 按查询条件读取借用单列表。
     *
     * @param query 查询条件及数据范围
     * @return 符合条件的借用单列表
     */
    @Override
    public List<BorrowVO> list(BorrowQueryDTO query) {
        return campusBorrowMapper.list(query);
    }

    /**
     * 读取到期提醒小时数，默认24小时并限定在1至720小时。
     *
     * @return 有效范围内的配置值
     */
    private int remindHours() {
        int hours = 24;
        try {
            hours = Integer.parseInt(iSysConfigService.selectConfigByKey("campus.borrow.remindHours"));
        } catch (NumberFormatException ignored) {
            // 配置缺失或格式错误时使用默认的24小时。
        }
        return Math.max(1, Math.min(720, hours));
    }

    /**
     * 分页开始前扫描提醒候选借用单，逐单以独立事务复核状态并幂等发送提醒。
     *
     * @param query 查询条件及数据范围
     */
    @Override
    public void remind(BorrowQueryDTO query) {
        final int remindHours = remindHours();
        query.setRemindHours(remindHours);
        for (BorrowVO candidate : campusBorrowMapper.reminderCandidates(query)) {
            reminderTransaction.executeWithoutResult(
                    transaction -> {
                        campusAssetStateService.selectForChange(candidate.assetId);
                        BorrowVO b = campusBorrowMapper.selectForChange(candidate.borrowId);
                        if (b == null
                                || !Arrays.asList("3", "4").contains(b.status)
                                || b.actualReturnTime != null
                                || b.plannedEndTime == null) {
                            return;
                        }
                        campusAccessService.requireData(b.assetDeptId, b.applicantUserId);
                        long remaining = b.plannedEndTime.getTime() - System.currentTimeMillis();
                        if (remaining > remindHours * 60L * 60 * 1000) {
                            return;
                        }
                        String type = remaining < 0 ? "OVERDUE" : "DUE_SOON";
                        campusMessageService.notify(
                                b.applicantUserId,
                                "BORROW:" + b.borrowId + ":" + type,
                                type,
                                "BORROW",
                                b.borrowId,
                                remaining < 0 ? "借用已逾期" : "借用即将到期",
                                "借用单 " + b.borrowNo + " 请及时办理归还交接");
                    });
        }
    }

    /**
     * 查询借还交接列表。
     *
     * @param query 查询条件及数据范围
     * @return 符合条件的交接单列表
     */
    @Override
    public List<HandoverVO> handovers(BorrowQueryDTO query) {
        return campusBorrowMapper.handovers(query);
    }

    /**
     * 查询借用单详情并校验当前用户的数据访问权限。
     *
     * @param id 借用单编号
     * @return 已通过权限或存在性校验的借用单
     */
    @Override
    public BorrowVO detail(Long id) {
        BorrowVO b = exists(campusBorrowMapper.find(id));
        campusAccessService.requireData(b.assetDeptId, b.applicantUserId);
        return b;
    }

    /**
     * 查询单条交接记录并校验数据权限。
     *
     * @param id 交接单编号
     * @return 已通过权限校验的交接单
     */
    @Override
    public HandoverVO handover(Long id) {
        HandoverVO h = campusBorrowMapper.handover(id);
        check(h != null, "交接记录不存在");
        campusAccessService.requireData(h.assetDeptId, h.applicantUserId);
        return h;
    }

    /**
     * 校验审核权限与预约冲突后审核借用申请，同步凭证、历史及通知。
     *
     * @param id 借用单编号
     * @param approve 是否通过
     * @param opinion 处理意见
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    @Override
    public void audit(Long id, boolean approve, String opinion) {
        campusAccessService.requirePermission("campus:borrow:audit");
        BorrowVO initial = exists(campusBorrowMapper.find(id));
        AssetStateVO asset = campusAssetStateService.selectForChange(initial.assetId);
        BorrowVO b = exists(campusBorrowMapper.selectForChange(id));
        campusAccessService.requireDepartment(b.assetDeptId);
        campusAccessService.requireDepartment(asset.getDeptId());
        check("0".equals(b.status), "仅待审核申请可以审核");
        check(!Objects.equals(b.applicantUserId, SecurityUtils.getUserId()), "不能审核本人申请");
        if (approve) {
            check(
                    "1".equals(asset.getBorrowEnabled())
                            && "0".equals(asset.getConditionStatus())
                            && !"2".equals(asset.getAssetStatus())
                            && !"3".equals(asset.getAssetStatus()),
                    "资产当前不可预约");
            check(
                    b.plannedStartTime != null
                            && b.plannedEndTime != null
                            && b.plannedEndTime.after(b.plannedStartTime)
                            && b.plannedEndTime.getTime() > System.currentTimeMillis(),
                    "预约时段无效或已过期");
            check(campusBorrowMapper.conflicts(b) == 0, "预约时段冲突或资产存在逾期借用");
            check(
                    campusBorrowMapper.activeRepairs(b.assetId) == 0 && campusBorrowMapper.activeScraps(b.assetId) == 0,
                    "资产存在未结维修或处置申请");
        } else {
            required(opinion);
        }
        String reason = opinion == null || opinion.isBlank() ? "借用审核通过" : opinion;
        String before = campusAssetStateService.snapshot(b.assetId);
        changed(
                campusBorrowMapper.audit(
                        id,
                        approve ? "2" : "1",
                        opinion,
                        approve ? UUID.randomUUID().toString().replace("-", "") : null,
                        SecurityUtils.getUserId(),
                        SecurityUtils.getUsername()));
        campusAssetStateService.history(
                b.assetId,
                approve ? "BORROW_APPROVE" : "BORROW_REJECT",
                "BORROW",
                id,
                before,
                reason);
        notify(b, approve ? "APPROVED" : "REJECTED", "借用审核结果", approve ? "借用申请已批准" : opinion);
    }

    /**
     * 取消尚未交付的预约并关闭待处理交接，记录历史并通知申请人。
     *
     * @param id 借用单编号
     * @param reason 取消原因
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    @Override
    public void cancel(Long id, String reason) {
        campusAccessService.requirePermission("campus:borrow:audit");
        required(reason);
        BorrowVO initial = exists(campusBorrowMapper.find(id));
        AssetStateVO asset = campusAssetStateService.selectForChange(initial.assetId);
        BorrowVO b = exists(campusBorrowMapper.selectForChange(id));
        campusAccessService.requireDepartment(b.assetDeptId);
        campusAccessService.requireDepartment(asset.getDeptId());
        check(
                ("0".equals(b.status) || "2".equals(b.status)) && b.actualPickupTime == null,
                "仅未交付预约可以取消");
        String before = campusAssetStateService.snapshot(b.assetId);
        changed(
                campusBorrowMapper.cancel(
                        id,
                        b.status,
                        reason,
                        SecurityUtils.getUserId(),
                        SecurityUtils.getUsername()));
        campusBorrowMapper.closePendingHandovers(
                id, reason, SecurityUtils.getUserId(), SecurityUtils.getUsername());
        campusAssetStateService.history(b.assetId, "BORROW_CANCEL", "BORROW", id, before, reason);
        notify(b, "CANCELLED", "借用预约已取消", reason);
    }

    /**
     * 核对人员、凭证及单据状态后确认或驳回交接，同步借用和资产状态。
     *
     * @param id 交接单编号
     * @param approve 是否通过
     * @param condition 交接确认的完好状态
     * @param opinion 处理意见
     * @param voucher 电子凭证
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    @Override
    public void confirm(
            Long id, boolean approve, String condition, String opinion, String voucher) {
        campusAccessService.requirePermission("campus:handover:confirm");
        HandoverVO initial = campusBorrowMapper.handover(id);
        check(initial != null, "交接记录不存在");
        AssetStateVO asset = campusAssetStateService.selectForChange(initial.assetId);
        BorrowVO b = exists(campusBorrowMapper.selectForChange(initial.borrowId));
        HandoverVO h = campusBorrowMapper.selectHandoverForChange(id);
        check(h != null && "0".equals(h.status), "交接记录已处理");
        campusAccessService.requireDepartment(b.assetDeptId);
        campusAccessService.requireDepartment(asset.getDeptId());
        check(Objects.equals(h.requestUserId, b.applicantUserId), "交接人与借用人不一致");
        check(!Objects.equals(b.applicantUserId, SecurityUtils.getUserId()), "不能确认本人交接");
        boolean pickup = "1".equals(h.handoverType);
        check(pickup || "2".equals(h.handoverType), "交接类型无效");
        check((pickup ? "2" : "4").equals(b.status), "借用单状态不允许交接");
        String before = campusAssetStateService.snapshot(b.assetId);
        if (approve) {
            check(
                    condition != null && Arrays.asList("0", "1", "2").contains(condition),
                    "请填写有效完好情况");
            if (!"0".equals(condition)) {
                required(opinion);
            }
            check(voucher != null && voucher.equals(b.voucherToken), "电子凭证不匹配");
            if (pickup) {
                check(
                        "0".equals(asset.getAssetStatus())
                                && "0".equals(asset.getConditionStatus())
                                && "0".equals(condition)
                                && "1".equals(asset.getBorrowEnabled()),
                        "资产不符合领取条件");
                check(
                        b.plannedStartTime != null
                                && b.plannedEndTime != null
                                && b.plannedStartTime.getTime() <= System.currentTimeMillis()
                                && b.plannedEndTime.getTime() > System.currentTimeMillis(),
                        "不在预约领取时段内");
                check(
                        campusBorrowMapper.activeRepairs(b.assetId) == 0
                                && campusBorrowMapper.activeScraps(b.assetId) == 0
                                && campusBorrowMapper.conflicts(b) == 0
                                && campusBorrowMapper.unreturned(b.assetId, b.borrowId) == 0,
                        "资产存在冲突业务或尚未归还的借用");
                campusAssetStateService.changeState(b.assetId, asset.getAssetStatus(), "1", "0");
            } else {
                check(
                        "1".equals(asset.getAssetStatus()) || "2".equals(asset.getAssetStatus()),
                        "资产不处于借用履行状态");
                boolean repairing = campusBorrowMapper.activeRepairs(b.assetId) > 0;
                // 交接时观察为完好，不能覆盖尚未解除的资产损坏状态。
                String resultingCondition =
                        "0".equals(condition) ? asset.getConditionStatus() : condition;
                campusAssetStateService.changeState(
                        b.assetId,
                        asset.getAssetStatus(),
                        repairing ? "2" : "0",
                        resultingCondition);
            }
            changed(
                    campusBorrowMapper.transition(
                            b.borrowId, b.status, pickup ? "3" : "5", SecurityUtils.getUsername()));
        } else {
            required(opinion);
            if (!pickup) {
                changed(campusBorrowMapper.transition(b.borrowId, "4", "3", SecurityUtils.getUsername()));
            }
        }
        changed(
                campusBorrowMapper.confirm(
                        id,
                        approve ? "1" : "2",
                        approve ? condition : null,
                        opinion,
                        SecurityUtils.getUserId(),
                        SecurityUtils.getUsername()));
        campusAssetStateService.history(
                b.assetId,
                approve ? (pickup ? "PICKUP" : "RETURN") : "HANDOVER_REJECT",
                "BORROW",
                b.borrowId,
                before,
                opinion == null || opinion.isBlank() ? "管理员确认交接" : opinion);
        campusMessageService.notify(
                b.applicantUserId,
                "HANDOVER:" + id + ":" + (approve ? "CONFIRM" : "REJECT"),
                pickup ? "PICKUP" : "RETURN",
                "BORROW",
                b.borrowId,
                "借还交接结果",
                approve ? "交接已确认" : opinion);
    }

    /**
     * 按幂等事件键写入业务通知，随调用方业务事务一同提交。
     *
     * @param b 借用单
     * @param action 业务操作类型
     * @param title 通知标题
     * @param content 业务说明或消息正文
     */
    private void notify(BorrowVO b, String action, String title, String content) {
        campusMessageService.notify(
                b.applicantUserId,
                "BORROW:" + b.borrowId + ":" + action,
                "BORROW_AUDIT",
                "BORROW",
                b.borrowId,
                title,
                content);
    }

    /**
     * 校验借用单存在并返回该单据。
     *
     * @param b 借用单
     * @return 已通过权限或存在性校验的借用单
     */
    private static BorrowVO exists(BorrowVO b) {
        check(b != null, "借用单不存在");
        return b;
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
