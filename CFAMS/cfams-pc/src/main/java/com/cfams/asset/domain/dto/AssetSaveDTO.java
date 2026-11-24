package com.cfams.asset.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.*;

/**
 * 台账维护允许写入的字段，不含流程状态、二维码及审计字段。
 */
@Getter
@Setter
public class AssetSaveDTO {
    private Long assetId;
    @NotBlank @Size(max = 64)
    private String assetCode;
    @NotBlank @Size(max = 150)
    private String assetName;
    @NotNull @Positive
    private Long categoryId;
    @Size(max = 200)
    private String specification;
    @PastOrPresent
    private LocalDate purchaseDate;
    @NotNull @DecimalMin("0") @Digits(integer = 12, fraction = 2)
    private BigDecimal purchasePrice;
    @Size(max = 150)
    private String supplierName;
    @DecimalMin(value = "0", inclusive = false) @Digits(integer = 3, fraction = 2)
    private BigDecimal expectedUseYears;
    @NotNull @Positive
    private Long deptId;
    @NotNull @Positive
    private Long locationId;
    @NotNull @Positive
    private Long keeperUserId;
    @NotNull @Pattern(regexp = "[01]")
    private String borrowEnabled = "0";
    @NotNull @Pattern(regexp = "[01]")
    private String visibility = "1";
    @Size(max = 500)
    private String remark;

}
