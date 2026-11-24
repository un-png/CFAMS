package com.cfams.asset.domain.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.*;

/** 类别维护参数及查询结果。根节点 parentId 为 null。 */
@Getter
@Setter
public class CategorySaveDTO {
    private Long categoryId;
    @Positive private Long parentId;
    @NotBlank @Size(max = 32) private String categoryCode;
    @NotBlank @Size(max = 100) private String categoryName;
    @NotNull @Min(0) private Integer orderNum = 0;
    @NotNull @Pattern(regexp = "[01]") private String status = "0";
    @Size(max = 500) private String remark;

}
