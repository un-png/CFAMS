package com.cfams.inventory.domain.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 草稿范围预览，用于逐件分配执行人。
 */
@Getter
@Setter
public class InventoryAssetVO {

    private Long assetId;

    private String assetCode;

    private String assetName;

    private Long deptId;

    private Long locationId;

    private Long categoryId;

    private Long keeperUserId;

}
