package com.cfams.inventory.domain.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.*;

/**
 * 复核仅接受明确结论；现场结果由小程序另行提交。
 */
@Getter
@Setter
public final class InventoryReviewDTO {

    @NotNull private Boolean approved;

    @NotBlank
    @Size(max = 1000)
    private String reviewNote;

    private Boolean recordOnly;

    @Positive private Long resolvedLocationId;

    @Pattern(regexp = "[012]")
    private String resolvedCondition;

    @Positive private Long repairId;

    private Boolean createRepair;

    @Size(max = 32)
    private String faultType;

    @Size(max = 2000)
    private String faultDescription;

    @Size(max = 200)
    private String faultLocation;

    /**
     * 创建空参数对象，供请求绑定或属性填充使用。
     */
    public InventoryReviewDTO() {
    }

    /**
     * 按原有参数顺序创建对象，保持构造调用兼容。
     */
    public InventoryReviewDTO(
            Boolean approved,
            String reviewNote,
            Boolean recordOnly,
            Long resolvedLocationId,
            String resolvedCondition,
            Long repairId,
            Boolean createRepair,
            String faultType,
            String faultDescription,
            String faultLocation) {
        this.approved = approved;
        this.reviewNote = reviewNote;
        this.recordOnly = recordOnly;
        this.resolvedLocationId = resolvedLocationId;
        this.resolvedCondition = resolvedCondition;
        this.repairId = repairId;
        this.createRepair = createRepair;
        this.faultType = faultType;
        this.faultDescription = faultDescription;
        this.faultLocation = faultLocation;
    }

}
