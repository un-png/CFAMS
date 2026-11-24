package com.cfams.inventory.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 盘点明细查询参数，增加任务、复核状态与差异筛选。
 */
@Getter
@Setter
public class InventoryItemQueryDTO extends InventoryTaskQueryDTO {

    private Long taskId;

    private String reviewStatus;

    private String difference;

}
