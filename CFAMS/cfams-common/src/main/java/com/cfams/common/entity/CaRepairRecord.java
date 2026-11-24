package com.cfams.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * ca_repair_record 持久化实体。
 * 仅用于数据映射；外部写入必须使用独立白名单DTO。
 */
@Getter
@Setter
public class CaRepairRecord {
    /** 主键 */
    public Long recordId;
    /** 关联 ca_repair */
    public Long repairId;
    /** ACCEPT、REJECT、ASSIGN、START、PROGRESS、SUBMIT、RETURN、VERIFY */
    public String actionType;
    /** 动作前维修单状态 */
    public String fromStatus;
    /** 动作后维修单状态；进度更新可相同 */
    public String toStatus;
    /** 操作者，关联 sys_user */
    public Long operatorUserId;
    /** 派工、改派的目标维修人员 */
    public Long targetUserId;
    /** 本次处理说明或方案 */
    public String content;
    /** 本次提交的累计费用快照，不作为独立费用求和 */
    public BigDecimal costSnapshot;
    /** 提交或核验时维修结果快照，1已修复；2无法修复 */
    public String resultSnapshot;
    /** 实际操作时间 */
    public Date actionTime;

}
