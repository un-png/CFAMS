package com.cfams.inventory.domain.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * 发布时逐件指定执行人，清单必须完整覆盖当前范围。
 */
@Getter
@Setter
public final class InventoryPublishDTO {

    @NotEmpty
    @Size(max = 10000)
    private List<@NotNull @Valid Assignment> assignments;

    /**
     * 创建空参数对象，供请求绑定或属性填充使用。
     */
    public InventoryPublishDTO() {
    }

    /**
     * 按原有参数顺序创建对象，保持构造调用兼容。
     */
    public InventoryPublishDTO(List<@NotNull @Valid Assignment> assignments) {
        this.assignments = assignments;
    }

    @Getter

    @Setter

    public static final class Assignment {

        @NotNull @Positive private Long assetId;

        @NotNull @Positive private Long userId;

        /**
         * 创建空参数对象，供请求绑定或属性填充使用。
         */
        public Assignment() {
        }

        /**
         * 按原有参数顺序创建对象，保持构造调用兼容。
         */
        public Assignment(Long assetId, Long userId) {
            this.assetId = assetId;
            this.userId = userId;
        }

    }

}
