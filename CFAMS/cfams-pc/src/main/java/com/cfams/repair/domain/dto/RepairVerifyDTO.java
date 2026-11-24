package com.cfams.repair.domain.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 维修反馈结果核验请求。 */
@Getter
@Setter
public class RepairVerifyDTO {
    /** 是否通过。 */
    @NotNull private Boolean approve;

    /** 处理意见。 */
    @Size(max = 500)
    private String opinion;

}
