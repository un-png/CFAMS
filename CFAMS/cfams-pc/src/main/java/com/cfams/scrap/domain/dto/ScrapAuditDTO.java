package com.cfams.scrap.domain.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.*;

/**
 * 审核级别由接口决定，不允许客户端指定审核人。
 */
@Getter
@Setter
public final class ScrapAuditDTO {

    @NotNull private Boolean approved;

    @NotBlank
    @Size(max = 1000)
    private String opinion;

    /**
     * 创建空参数对象，供请求绑定或属性填充使用。
     */
    public ScrapAuditDTO() {
    }

    /**
     * 按原有参数顺序创建对象，保持构造调用兼容。
     */
    public ScrapAuditDTO(Boolean approved, String opinion) {
        this.approved = approved;
        this.opinion = opinion;
    }

}
