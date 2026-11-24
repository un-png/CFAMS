package com.cfams.asset.domain.vo;

import com.cfams.common.entity.CaCategory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** 基础配置查询视图；请求仍使用独立CategoryInput白名单。 */
@JsonIgnoreProperties({"createBy", "createTime", "updateBy", "updateTime"})
public class CategoryVO extends CaCategory {
}
