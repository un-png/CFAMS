package com.cfams.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ca_asset_history 持久化实体。
 * 仅用于数据映射；外部写入必须使用独立白名单DTO。
 */
@Getter
@Setter
public class CaAssetHistory {
    /** 主键 */
    public Long historyId;
    /** 关联 ca_asset */
    public Long assetId;
    /** REGISTER、EDIT、PICKUP、RETURN、REPAIR、INVENTORY、SCRAP */
    public String eventType;
    /** BORROW、REPAIR、INVENTORY_ITEM、SCRAP；直接台账维护为空 */
    public String bizType;
    /** 关联业务ID，与 biz_type 同时为空或同时非空 */
    public Long bizId;
    /** 变更前资产快照，首次入册为空 */
    public String beforeSnapshot;
    /** 变更后资产快照，结构见第7节 */
    public String afterSnapshot;
    /** 业务操作者，关联 sys_user */
    public Long operatorUserId;
    /** 变更时间 */
    public LocalDateTime eventTime;
    /** 变更原因 */
    public String changeReason;

}
