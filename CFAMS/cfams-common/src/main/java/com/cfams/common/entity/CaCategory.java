package com.cfams.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ca_category 持久化实体。
 * 仅用于数据映射；外部写入必须使用独立白名单DTO。
 */
@Getter
@Setter
public class CaCategory {
    /** 主键 */
    public Long categoryId;
    /** 父类别；根节点为空，关联本表 */
    public Long parentId;
    /** 唯一类别编码 */
    public String categoryCode;
    /** 类别名称 */
    public String categoryName;
    /** 同级显示顺序，非负 */
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
