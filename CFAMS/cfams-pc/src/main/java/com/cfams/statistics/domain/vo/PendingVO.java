package com.cfams.statistics.domain.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 授权范围内待处理记录数，不代表当前用户具备审批资格。
 */
@Getter
@Setter
public class PendingVO {

    private long borrowAudit;

    private long pickupHandover;

    private long returnHandover;

    private long repairAcceptance;

    private long repairVerification;

    private long inventoryReview;

    private long scrapAudit;

}
