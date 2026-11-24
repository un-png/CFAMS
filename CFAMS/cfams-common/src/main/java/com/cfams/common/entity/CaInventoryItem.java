package com.cfams.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ca_inventory_item 持久化实体。
 * 仅用于数据映射；外部写入必须使用独立白名单DTO。
 */
@Getter
@Setter
public class CaInventoryItem {
    /** 主键 */
    public Long itemId;
    /** 关联 ca_inventory_task */
    public Long taskId;
    /** 关联 ca_asset */
    public Long assetId;
    /** 负责执行人，必须属于该任务分配人员 */
    public Long assignedUserId;
    /** 发布时资产基准快照，结构见第7节 */
    public String baseSnapshot;
    /** 最后一次现场扫码时间；未找到时可为空 */
    public LocalDateTime scanTime;
    /** 现场结果提交人，关联 sys_user */
    public Long checkedUserId;
    /** 提交时间；为空表示尚未提交现场结果 */
    public LocalDateTime checkedTime;
    /** 现场位置，关联 ca_location；发现资产时必填 */
    public Long observedLocationId;
    /** 0正常；1损坏；2待核验，发现资产时必填 */
    public String observedCondition;
    /** 0未报告疑似丢失；1疑似丢失 */
    public String missingFlag;
    /** 现场说明，异常必填 */
    public String checkNote;
    /** 0待复核；1已复核；2退回重盘 */
    public String reviewStatus;
    /** 复核人，关联 sys_user */
    public Long reviewUserId;
    /** 复核时间 */
    public LocalDateTime reviewTime;
    /** 复核结论；异常、未盘结项或退回必填 */
    public String reviewNote;
    /** 复核时最新台账快照 */
    public String reviewSnapshot;
    /** 复核确认的位置；不变更时为空 */
    public Long resolvedLocationId;
    /** 复核确认的完好状态；不变更时为空 */
    public String resolvedCondition;
    /** 复核创建或关联的报修单 */
    public Long repairId;
    /** 确认丢失后关联的报废处置单 */
    public Long scrapId;
    /** 创建账号，服务端填写 */
    public String createBy;
    /** 创建时间，服务端填写 */
    public LocalDateTime createTime;
    /** 最后更新账号 */
    public String updateBy;
    /** 最后更新时间；不作为乐观锁版本 */
    public LocalDateTime updateTime;
    /** 补充说明，不替代结构化业务字段 */
    public String remark;

}
