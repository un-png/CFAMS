package com.cfams.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ca_scrap_audit 持久化实体。
 * 仅用于数据映射；外部写入必须使用独立白名单DTO。
 */
@Getter
@Setter
public class CaScrapAudit {
    /** 主键 */
    public Long auditId;
    /** 关联 ca_scrap */
    public Long scrapId;
    /** 1初审；2复审 */
    public String auditLevel;
    /** 1通过；2驳回 */
    public String auditResult;
    /** 审核人，关联 sys_user */
    public Long auditorUserId;
    /** 审核意见 */
    public String auditOpinion;
    /** 审核时间 */
    public LocalDateTime auditTime;

}
