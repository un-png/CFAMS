package com.cfams.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * ca_borrow 持久化实体。
 * 仅用于数据映射；外部写入必须使用独立白名单DTO。
 */
@Getter
@Setter
public class CaBorrow {
    /** 主键 */
    public Long borrowId;
    /** 唯一业务单号 */
    public String borrowNo;
    /** 关联 ca_asset */
    public Long assetId;
    /** 申请人，关联 sys_user */
    public Long applicantUserId;
    /** 申请时人员部门快照，关联 sys_dept */
    public Long applicantDeptId;
    /** 申请时资产部门快照，供该单据权限与历史查询 */
    public Long assetDeptId;
    /** 借用用途 */
    public String purpose;
    /** 预计领取时间 */
    public Date plannedStartTime;
    /** 预计归还时间，严格晚于预计领取时间 */
    public Date plannedEndTime;
    /** 0待审核；1已驳回；2待领取；3借用中；4待归还确认；5已完成；6已取消 */
    public String status;
    /** 提交时间 */
    public Date applyTime;
    /** 审核人，关联 sys_user */
    public Long auditUserId;
    /** 审核时间 */
    public Date auditTime;
    /** 审核意见，驳回必填 */
    public String auditOpinion;
    /** 批准后生成的唯一电子凭证，大小写敏感 */
    public String voucherToken;
    /** 管理员确认领取时间 */
    public Date actualPickupTime;
    /** 管理员确认归还时间 */
    public Date actualReturnTime;
    /** 取消操作人；仅未交付前允许取消 */
    public Long cancelUserId;
    /** 取消时间 */
    public Date cancelTime;
    /** 取消原因 */
    public String cancelReason;
    /** 创建账号，服务端填写 */
    public String createBy;
    /** 创建时间，服务端填写 */
    public Date createTime;
    /** 最后更新账号 */
    public String updateBy;
    /** 最后更新时间；不作为乐观锁版本 */
    public Date updateTime;
    /** 补充说明，不替代结构化业务字段 */
    public String remark;

}
