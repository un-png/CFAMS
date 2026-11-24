package com.cfams.asset.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import com.cfams.asset.service.CampusAssetStateService;
import com.cfams.asset.domain.vo.AssetStateVO;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.cfams.common.exception.ServiceException;
import com.cfams.common.utils.SecurityUtils;
import com.cfams.asset.mapper.CampusAssetMapper;

/** 跨业务资产状态和履历，调用方必须开启同一事务。 */
@Service
public class CampusAssetStateServiceImpl implements CampusAssetStateService {
    @Autowired
    private CampusAssetMapper campusAssetMapper;

    /** 在当前业务事务内读取资产，供借还、维修和报废流程校验当前状态。 */
    @Override
    public AssetStateVO selectForChange(Long id) {
        requireTransaction();
        AssetStateVO asset = campusAssetMapper.selectAssetForChange(id);
        if (asset == null) throw new ServiceException("资产不存在");
        return asset;
    }
    /** 读取资产当前关键字段的JSON快照，用于记录变更前后状态。 */
    @Override
    public String snapshot(Long id) {
        return campusAssetMapper.snapshot(id);
    }
    /** 在当前事务内写入资产变更前后快照、操作人及关联业务记录。 */
    @Override
    public void history(Long id, String event, String bizType, Long bizId, String before, String reason) {
        requireTransaction();
        String after = snapshot(id);
        if (after == null) throw new ServiceException("无法记录不存在的资产履历");
        campusAssetMapper.insertHistory(id, event, bizType, bizId, before, after, SecurityUtils.getUserId(),
                reason == null || reason.trim().isEmpty() ? event : reason);
    }
    /** 按预期旧状态更新资产状态与完好情况，状态已变化时拒绝提交。 */
    @Override
    public void changeState(Long id, String expectedStatus, String newStatus, String condition) {
        requireTransaction();
        if (newStatus == null || !"0123".contains(newStatus) || newStatus.length() != 1
                || condition == null || condition.length() != 1 || !"012".contains(condition)) {
            throw new ServiceException("无效的资产状态");
        }
        if (campusAssetMapper.changeState(id, expectedStatus, newStatus, condition, SecurityUtils.getUsername()) != 1) {
            throw new ServiceException("资产状态已发生变化，请刷新后重试");
        }
    }
    /** 确保资产读取、状态更新和履历写入均处于业务事务内。 */
    private void requireTransaction() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("资产状态修改必须位于业务事务内");
        }
    }
}
