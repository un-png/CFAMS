package com.cfams.repair.domain.vo;

import lombok.Getter;
import lombok.Setter;

import com.cfams.common.entity.CaRepair;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** 查询投影：持久化字段继承实体，附加展示字段；保持原JSON字段集合。 */
@JsonIgnoreProperties({"createBy", "createTime", "updateBy", "updateTime", "remark"})
@Getter
@Setter
public class RepairVO extends CaRepair {

    public String assetCode;

    public String assetName;

    public String reporterName;

    public String repairerName;

}
