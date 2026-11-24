package com.cfams.asset.domain.vo;

import lombok.Getter;
import lombok.Setter;

import com.cfams.common.entity.CaAsset;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** 查询投影：持久化字段继承实体，附加展示字段；保持原JSON字段集合。 */
@JsonIgnoreProperties({"createBy", "createTime", "updateBy", "updateTime"})
@Getter
@Setter
public class AssetVO extends CaAsset {
    private String categoryName;

    private String locationName;

    private String deptName;

    private String keeperName;

}
