package com.cfams.scrap.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.cfams.campus.service.CampusAccessService;
import com.cfams.asset.service.CampusAssetStateService;
import com.cfams.asset.domain.vo.AssetStateVO;
import com.cfams.common.exception.ServiceException;
import com.cfams.common.utils.SecurityUtils;
import com.cfams.message.service.CampusMessageService;
import com.cfams.scrap.domain.dto.*;
import com.cfams.scrap.domain.vo.*;
import com.cfams.scrap.mapper.CampusScrapMapper;
import com.cfams.scrap.service.CampusScrapService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 先读取资产再读取处置单，审批、台账、履历及通知同事务提交。
 */
@Service
public class CampusScrapServiceImpl implements CampusScrapService {

    @Autowired
    private CampusScrapMapper campusScrapMapper;

    @Autowired
    private CampusAccessService campusAccessService;

    @Autowired
    private CampusAssetStateService campusAssetStateService;

    @Autowired
    private CampusMessageService campusMessageService;

    /**
     * 按查询条件和数据权限查询处置单列表。
     */
    @Override
    public List<ScrapVO> list(ScrapQueryDTO query) {
        campusAccessService.scope(query);
        return campusScrapMapper.list(query);
    }

    /**
     * 查询处置详情、审核记录及证明附件并校验访问权限。
     */
    @Override
    public ScrapVO detail(Long id) {
        ScrapVO scrap = required(campusScrapMapper.detail(id));
        campusAccessService.requireData(scrap.getAssetDeptId(), scrap.getApplicantUserId());
        scrap.setAudits(campusScrapMapper.audits(id));
        scrap.setAttachments(campusScrapMapper.attachments(id));
        return scrap;
    }

    /**
     * 校验资产及未完成业务后创建处置申请。
     */
    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Long create(ScrapCreateDTO request) {
        campusAccessService.requirePermission("campus:scrap:add");
        AssetStateVO asset = campusAssetStateService.selectForChange(request.getAssetId());
        campusAccessService.requireDepartment(asset.getDeptId());
        requireDisposable(asset, null);
        ScrapVO scrap = new ScrapVO();
        scrap.setScrapNo("SC" + UUID.randomUUID().toString().replace("-", ""));
        scrap.setAssetId(asset.getAssetId());
        scrap.setAssetDeptId(asset.getDeptId());
        scrap.setApplicantUserId(SecurityUtils.getUserId());
        scrap.setDisposeType(request.getDisposeType());
        scrap.setReason(request.getReason());
        scrap.setCreateBy(SecurityUtils.getUsername());
        campusScrapMapper.insert(scrap);
        return scrap.getScrapId();
    }

    /**
     * 执行固定两级审核，终审通过时注销资产并记录履历和通知。
     */
    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void audit(Long id, String level, ScrapAuditDTO request) {
        if (!List.of("1", "2").contains(level)) {
            throw new ServiceException("审核级别无效");
        }
        campusAccessService.requirePermission("campus:scrapAudit:" + ("1".equals(level) ? "firstAudit" : "finalAudit"));
        ScrapVO initial = required(campusScrapMapper.detail(id));
        AssetStateVO asset = campusAssetStateService.selectForChange(initial.getAssetId());
        ScrapVO scrap = required(campusScrapMapper.selectForChange(id));
        if (!Objects.equals(initial.getAssetId(), scrap.getAssetId())) {
            throw new ServiceException("处置单关联资产已变化");
        }
        campusAccessService.requireDepartment(scrap.getAssetDeptId());
        campusAccessService.requireDepartment(asset.getDeptId());
        String expected = "1".equals(level) ? "0" : "1";
        if (!expected.equals(scrap.getStatus())) {
            throw new ServiceException("单据状态已变化，请刷新");
        }
        Long userId = SecurityUtils.getUserId();
        if (Objects.equals(userId, scrap.getApplicantUserId())) {
            throw new ServiceException("申请人不得审核自己的单据");
        }
        if ("2".equals(level)) {
            ScrapAuditVO first =
                    campusScrapMapper.audits(id).stream()
                            .filter(a -> "1".equals(a.getAuditLevel()) && "1".equals(a.getAuditResult()))
                            .findFirst()
                            .orElseThrow(() -> new ServiceException("尚未通过初审"));
            if (Objects.equals(first.getAuditorUserId(), userId)) {
                throw new ServiceException("初审和复审必须由不同人员办理");
            }
        }
        if (request.getApproved()) {
            requireDisposable(asset, id);
            if (campusScrapMapper.selectProofIds(id).isEmpty()) {
                throw new ServiceException("请先上传真实处置证明附件");
            }
        }
        String target = !request.getApproved() ? "2" : ("1".equals(level) ? "1" : "3");
        if (campusScrapMapper.transition(id, expected, target, SecurityUtils.getUsername()) != 1) {
            throw new ServiceException("单据状态已变化，请刷新");
        }
        ScrapAuditVO audit = new ScrapAuditVO();
        audit.setScrapId(id);
        audit.setAuditLevel(level);
        audit.setAuditResult(request.getApproved() ? "1" : "2");
        audit.setAuditorUserId(userId);
        audit.setAuditOpinion(request.getOpinion());
        campusScrapMapper.insertAudit(audit);
        if ("3".equals(target)) {
            String before = campusAssetStateService.snapshot(asset.getAssetId());
            campusAssetStateService.changeState(
                    asset.getAssetId(), asset.getAssetStatus(), "3", asset.getConditionStatus());
            campusAssetStateService.history(asset.getAssetId(), "SCRAP", "SCRAP", id, before, request.getOpinion());
        }
        campusMessageService.notify(
                scrap.getApplicantUserId(),
                "scrap_audit:" + audit.getAuditId(),
                "SCRAP",
                "SCRAP",
                id,
                "报废审核结果",
                "处置单" + scrap.getScrapNo() + ("2".equals(target) ? "已驳回" : "审核已通过"));
    }

    /**
     * 校验资产未注销、未借出、未维修且不存在冲突业务。
     */
    private void requireDisposable(AssetStateVO asset, Long excludeId) {
        if ("3".equals(asset.getAssetStatus())) {
            throw new ServiceException("资产已注销");
        }
        if (!"0".equals(asset.getAssetStatus())) {
            throw new ServiceException("借出或维修中的资产不能处置");
        }
        if (campusScrapMapper.unfinishedBorrow(asset.getAssetId()) > 0) {
            throw new ServiceException("资产存在未完成借还业务");
        }
        if (campusScrapMapper.unfinishedRepair(asset.getAssetId()) > 0) {
            throw new ServiceException("资产存在未完成维修业务");
        }
        if (campusScrapMapper.pendingScrap(asset.getAssetId(), excludeId) > 0) {
            throw new ServiceException("资产已有待审处置单");
        }
    }

    /**
     * 校验处置单存在并返回单据。
     */
    private ScrapVO required(ScrapVO scrap) {
        if (scrap == null) {
            throw new ServiceException("处置单不存在");
        }
        return scrap;
    }
}
