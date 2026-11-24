package com.cfams.asset.domain.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.*;

/** 场所维护参数。公共场所管理部门可为空，仅全范围管理员维护。 */
@Getter
@Setter
public class LocationSaveDTO {
    private Long locationId;
    @Positive private Long parentId;
    @NotBlank @Size(max = 32) private String locationCode;
    @NotBlank @Size(max = 100) private String locationName;
    @NotNull @Pattern(regexp = "[1-5]") private String locationType;
    @Positive private Long deptId;
    @NotNull @Min(0) private Integer orderNum = 0;
    @NotNull @Pattern(regexp = "[01]") private String status = "0";
    @Size(max = 500) private String remark;

}
