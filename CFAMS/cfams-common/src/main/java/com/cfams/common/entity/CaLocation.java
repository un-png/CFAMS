package com.cfams.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ca_location 持久化实体。
 * 仅用于数据映射；外部写入必须使用独立白名单DTO。
 */
@Getter
@Setter
public class CaLocation {
    /** 主键 */
    public Long locationId;
    /** 上级场所，关联本表 */
    public Long parentId;
    /** 唯一场所编码 */
    public String locationCode;
    /** 场所名称 */
    public String locationName;
    /** 1校区；2楼栋；3楼层；4房间；5其他 */
    public String locationType;
    /** 管理部门，关联 sys_dept；公共场所可为空 */
    public Long deptId;
    /** 显示顺序 */
    public Integer orderNum;
    /** 0正常；1停用 */
    public String status;
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
