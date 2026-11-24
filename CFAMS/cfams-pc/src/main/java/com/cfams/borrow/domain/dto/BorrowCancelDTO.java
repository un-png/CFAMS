package com.cfams.borrow.domain.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 取消未交付借用预约的请求。 */
@Getter
@Setter
public class BorrowCancelDTO {
    /** 取消原因。 */
    @NotBlank
    @Size(max = 500)
    private String reason;

}
