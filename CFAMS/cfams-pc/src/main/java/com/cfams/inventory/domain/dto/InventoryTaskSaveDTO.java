package com.cfams.inventory.domain.dto;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PC盘点任务草稿；发布后不允许修改范围及人员。
 */
@Getter
@Setter
public final class InventoryTaskSaveDTO {

    @NotBlank
    @Size(max = 150)
    private String taskName;

    @NotNull @Positive private Long deptId;

    @Positive private Long scopeDeptId;

    @Positive private Long scopeLocationId;

    @Positive private Long scopeCategoryId;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @NotEmpty
    @Size(max = 500)
    private List<@NotNull @Positive Long> userIds;

    @Size(max = 500)
    private String remark;

    /**
     * 创建空参数对象，供请求绑定或属性填充使用。
     */
    public InventoryTaskSaveDTO() {
    }

    /**
     * 按原有参数顺序创建对象，保持构造调用兼容。
     */
    public InventoryTaskSaveDTO(
            String taskName,
            Long deptId,
            Long scopeDeptId,
            Long scopeLocationId,
            Long scopeCategoryId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<@NotNull @Positive Long> userIds,
            String remark) {
        this.taskName = taskName;
        this.deptId = deptId;
        this.scopeDeptId = scopeDeptId;
        this.scopeLocationId = scopeLocationId;
        this.scopeCategoryId = scopeCategoryId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.userIds = userIds;
        this.remark = remark;
    }

}
