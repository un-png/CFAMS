package com.cfams.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ca_inventory_user 持久化实体。
 * 仅用于数据映射；外部写入必须使用独立白名单DTO。
 */
@Getter
@Setter
public class CaInventoryUser {
    /** 主键 */
    public Long taskUserId;
    /** 关联 ca_inventory_task */
    public Long taskId;
    /** 分配的执行人，关联 sys_user */
    public Long userId;
    /** 分配人，关联 sys_user */
    public Long assignedBy;
    /** 分配时间 */
    public LocalDateTime assignedTime;

}
