package com.cfams.inventory.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 盘点差异生成的真实报修单写入参数。
 */
@Getter
@Setter
public class InventoryRepair {

    private Long repairId;

    private String repairNo;

    private Long assetId;

    private Long reporterUserId;

    private Long assetDeptId;

    private String faultType;

    private String faultDescription;

    private String faultLocation;

    private LocalDateTime faultTime;

    private String createBy;

}
