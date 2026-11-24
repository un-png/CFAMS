package com.cfams.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * ca_handover 持久化实体。
 * 仅用于数据映射；外部写入必须使用独立白名单DTO。
 */
@Getter
@Setter
public class CaHandover {
    /** 主键 */
    public Long handoverId;
    /** 关联 ca_borrow */
    public Long borrowId;
    /** 1领取；2归还 */
    public String handoverType;
    /** 本次请求幂等键，重试使用同一值 */
    public String requestKey;
    /** 申请交接人，必须为借用申请人 */
    public Long requestUserId;
    /** 扫码登记或归还申请时间 */
    public Date requestTime;
    /** 用户说明 */
    public String requestNote;
    /** 0待确认；1已确认；2已拒绝 */
    public String status;
    /** 管理员，关联 sys_user */
    public Long confirmUserId;
    /** 确认或拒绝时间 */
    public Date confirmTime;
    /** 现场核验完好情况，含义同 ca_asset；确认时必填 */
    public String conditionStatus;
    /** 交接意见；拒绝或异常归还时必填 */
    public String confirmNote;
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
