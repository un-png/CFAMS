package com.cfams.asset.service;

import com.cfams.asset.domain.vo.AssetStateVO;

/** 资产流程状态与履历服务，供借还、维修、盘点和报废复用。 */
public interface CampusAssetStateService {
    /** 在现有业务事务内读取资产状态，不存在时拒绝继续办理。 */
    AssetStateVO selectForChange(Long id);

    /** 读取资产当前关键字段的JSON快照，用于记录变更前后状态。 */
    String snapshot(Long id);

    /** 在当前事务内保存资产前后快照、关联业务及操作原因。 */
    void history(Long id, String event, String bizType, Long bizId, String before, String reason);

    /** 按预期旧状态更新资产状态与完好情况，更新失败时拒绝提交。 */
    void changeState(Long id, String expectedStatus, String newStatus, String condition);
}
