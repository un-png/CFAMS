package com.cfams.asset.domain.vo;

import com.cfams.common.entity.CaLocation;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** 基础配置查询视图；请求仍使用独立LocationInput白名单。 */
@JsonIgnoreProperties({"createBy", "createTime", "updateBy", "updateTime"})
public class LocationVO extends CaLocation {
}
