package com.cfams.borrow.domain.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 借还交接确认请求。 */
@Getter
@Setter
public class HandoverConfirmDTO {
    /** 是否通过。 */
    @NotNull private Boolean approve;

    /** 处理意见。 */
    @Size(max = 500)
    private String opinion;

    /** 资产完好状态。 */
    @Size(max = 1)
    private String conditionStatus;

    /** 电子凭证令牌。 */
    @Size(max = 64)
    private String voucherToken;

}
