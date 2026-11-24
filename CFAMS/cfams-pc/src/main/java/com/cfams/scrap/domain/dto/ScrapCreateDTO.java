package com.cfams.scrap.domain.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.*;

/**
 * 申请人及部门由服务器取得，附件在取得单据ID后上传。
 */
@Getter
@Setter
public final class ScrapCreateDTO {

    @NotNull @Positive private Long assetId;

    @NotBlank
    @Pattern(regexp = "[12]")
    private String disposeType;

    @NotBlank
    @Size(max = 2000)
    private String reason;

    /**
     * 创建空参数对象，供请求绑定或属性填充使用。
     */
    public ScrapCreateDTO() {
    }

    /**
     * 按原有参数顺序创建对象，保持构造调用兼容。
     */
    public ScrapCreateDTO(Long assetId, String disposeType, String reason) {
        this.assetId = assetId;
        this.disposeType = disposeType;
        this.reason = reason;
    }

}
