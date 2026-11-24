package com.cfams.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ca_scrap 持久化实体。
 * 仅用于数据映射；外部写入必须使用独立白名单DTO。
 */
@Getter
@Setter
public class CaScrap {
    /** 主键 */
    public Long scrapId;
    /** 唯一处置单号 */
    public String scrapNo;
    /** 关联 ca_asset */
    public Long assetId;
    /** 申请时资产部门快照 */
    public Long assetDeptId;
    /** 申请人，关联 sys_user */
    public Long applicantUserId;
    /** 1报废；2确认丢失后的注销 */
    public String disposeType;
    /** 处置原因；丢失注销还需核查结论 */
    public String reason;
    /** 申请时间 */
    public LocalDateTime applyTime;
    /** 0待初审；1待复审；2已驳回；3已注销 */
    public String status;
    /** 复审通过与注销时间 */
    public LocalDateTime completedTime;
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
