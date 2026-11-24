package com.cfams.identity.domain.dto;

import lombok.Getter;
import lombok.Setter;

import com.cfams.common.annotation.Excel;
import jakarta.validation.constraints.*;

/** 人员维护和名册导入参数；不接受绑定摘要和微信标识。 */
@Getter
@Setter
public class PersonSaveDTO {
    @NotBlank @Size(max = 32) @Excel(name = "学号或工号")
    private String personNo;
    @NotBlank @Pattern(regexp = "[12]") @Excel(name = "人员类型", readConverterExp = "1=学生,2=教职工")
    private String personType;
    @NotBlank @Size(max = 50) @Excel(name = "姓名")
    private String realName;
    @NotNull @Positive @Excel(name = "部门ID")
    private Long deptId;
    @Positive @Excel(name = "关联用户ID")
    private Long userId;
    @NotBlank @Pattern(regexp = "[01]") @Excel(name = "状态", readConverterExp = "0=有效,1=停用")
    private String status = "0";
    @Size(max = 500) @Excel(name = "备注")
    private String remark;

}
