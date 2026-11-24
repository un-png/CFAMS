package com.cfams.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * ca_repair 持久化实体。
 * 仅用于数据映射；外部写入必须使用独立白名单DTO。
 */
@Getter
@Setter
public class CaRepair {
    /** 主键 */
    public Long repairId;
    /** 唯一报修单号 */
    public String repairNo;
    /** 关联 ca_asset */
    public Long assetId;
    /** 报修时仍在履行的借用单，关联 ca_borrow */
    public Long borrowId;
    /** 报修人，关联 sys_user */
    public Long reporterUserId;
    /** 报修时资产归属部门快照 */
    public Long assetDeptId;
    /** 故障类型，取 ca_fault_type 字典值 */
    public String faultType;
    /** 故障描述 */
    public String faultDescription;
    /** 故障发生时间 */
    public Date faultTime;
    /** 故障发生地点文字，允许描述登记场所以外的位置 */
    public String faultLocation;
    /** 提交报修时间 */
    public Date reportTime;
    /** 0待受理；1已派工；2维修中；3待核验；4已完成；5已驳回 */
    public String status;
    /** 受理或驳回管理员 */
    public Long acceptUserId;
    /** 受理或驳回时间 */
    public Date acceptTime;
    /** 受理意见；驳回必填 */
    public String acceptOpinion;
    /** 当前维修人员，关联 sys_user */
    public Long repairerUserId;
    /** 最近派工时间 */
    public Date assignTime;
    /** 首次接单开始时间 */
    public Date startTime;
    /** 最新维修方案 */
    public String repairPlan;
    /** 核验的实际总费用，非负；尚未反馈为空 */
    public BigDecimal repairCost;
    /** 1已修复；2无法修复 */
    public String repairResult;
    /** 维修结果描述 */
    public String resultDescription;
    /** 最后一次提交核验时间 */
    public Date feedbackTime;
    /** 最终核验人，关联 sys_user */
    public Long verifyUserId;
    /** 最终核验通过时间 */
    public Date verifyTime;
    /** 最终核验意见 */
    public String verifyOpinion;
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
