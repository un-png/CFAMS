package com.cfams.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ca_inventory_task 持久化实体。
 * 仅用于数据映射；外部写入必须使用独立白名单DTO。
 */
@Getter
@Setter
public class CaInventoryTask {
    /** 主键 */
    public Long taskId;
    /** 唯一任务编号 */
    public String taskNo;
    /** 任务名称 */
    public String taskName;
    /** 任务管理部门，关联 sys_dept */
    public Long deptId;
    /** 范围部门筛选，关联 sys_dept；含下级 */
    public Long scopeDeptId;
    /** 范围场所筛选，关联 ca_location；含下级 */
    public Long scopeLocationId;
    /** 范围类别筛选，关联 ca_category；含下级 */
    public Long scopeCategoryId;
    /** 计划开始时间 */
    public LocalDateTime startTime;
    /** 计划截止时间，晚于开始时间 */
    public LocalDateTime endTime;
    /** 0未开始；1执行中；2待复核；3已完成 */
    public String status;
    /** 发布人，关联 sys_user */
    public Long publisherUserId;
    /** 发布时刻；NULL表示尚未发布 */
    public LocalDateTime publishTime;
    /** 提交整项任务复核时间 */
    public LocalDateTime submitTime;
    /** 任务结项人 */
    public Long completeUserId;
    /** 任务完成时间 */
    public LocalDateTime completeTime;
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
