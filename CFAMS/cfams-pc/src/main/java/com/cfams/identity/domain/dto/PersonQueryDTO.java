package com.cfams.identity.domain.dto;

import lombok.Getter;
import lombok.Setter;

import com.cfams.campus.domain.dto.CampusQueryDTO;

/** 人员名册查询；按人员类型、部门、启停状态及绑定状态筛选。 */
@Getter
@Setter
public class PersonQueryDTO extends CampusQueryDTO {
    private String personType;
    private String bindingState;

}
