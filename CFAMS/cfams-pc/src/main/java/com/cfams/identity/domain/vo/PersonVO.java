package com.cfams.identity.domain.vo;

import lombok.Getter;
import lombok.Setter;

import com.cfams.common.entity.CaPerson;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** 查询投影：持久化字段继承实体，附加展示字段；保持原JSON字段集合。 */
@JsonIgnoreProperties({"bindTokenHash", "tokenExpireTime", "tokenUsedTime", "createBy", "updateBy", "updateTime"})
@Getter
@Setter
public class PersonVO extends CaPerson {
    private String deptName;
    private String userName;
    private String bindingState;

}
