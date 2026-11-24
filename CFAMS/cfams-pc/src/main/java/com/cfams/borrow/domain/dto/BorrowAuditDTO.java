package com.cfams.borrow.domain.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 借用申请审核请求。 */
@Getter
@Setter
public class BorrowAuditDTO {
    /** 是否通过。 */
    @NotNull private Boolean approve;

    /** 处理意见。 */
    @Size(max = 500)
    private String opinion;

}
