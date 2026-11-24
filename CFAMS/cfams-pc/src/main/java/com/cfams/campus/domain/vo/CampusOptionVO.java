package com.cfams.campus.domain.vo;

import lombok.Getter;
import lombok.Setter;

/** 业务表单的ID与名称选项，不包含账号敏感信息。 */
@Getter
@Setter
public class CampusOptionVO {
    private Long value;
    private String label;

    /** 创建供MyBatis填充的空选项。 */
    public CampusOptionVO() {
    }

    /** 使用业务主键及显示名称创建选项。 */
    public CampusOptionVO(Long value, String label) {
        this.value = value;
        this.label = label;
    }

}
