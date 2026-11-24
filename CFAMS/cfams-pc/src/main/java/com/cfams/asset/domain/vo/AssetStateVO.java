package com.cfams.asset.domain.vo;

import com.cfams.common.entity.CaAsset;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** 资产读取投影复用common实体，不作为外部修改请求。 */
@JsonIgnoreProperties({"specification", "purchaseDate", "supplierName", "expectedUseYears", "qrToken", "createBy", "createTime", "updateBy", "updateTime", "remark"})
public class AssetStateVO extends CaAsset {
}
