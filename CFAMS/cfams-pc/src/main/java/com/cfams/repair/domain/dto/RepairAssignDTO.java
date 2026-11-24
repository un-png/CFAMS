package com.cfams.repair.domain.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 报修受理、驳回或维修人员改派请求。 */
@Getter
@Setter
public class RepairAssignDTO {
    /** 是否通过。 */
    @NotNull private Boolean approve;

    /** 维修人用户编号。 */
    private Long repairerUserId;

    /** 处理意见。 */
    @Size(max = 500)
    private String opinion;

}
