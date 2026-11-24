package com.cfams.identity.domain.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.*;

/** 管理员线下核验后解除指定旧绑定，不接收任何微信身份字段。 */
@Getter
@Setter
public class BindingRebindDTO {
    @NotNull @Positive
    private Long bindingId;
    @NotBlank @Size(max = 32)
    private String personNo;
    @NotBlank @Size(max = 500)
    private String reason;

}
