package com.cfams.inventory.domain.vo;

import lombok.Getter;
import lombok.Setter;

import com.cfams.common.entity.CaInventoryItem;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 查询投影：持久化字段继承实体，附加展示字段；保持原JSON字段集合。
 */
@JsonIgnoreProperties({"createTime", "updateBy", "updateTime", "remark"})
@Getter
@Setter
public class InventoryItemVO extends CaInventoryItem {

    private String assetCode;

    private String assetName;

    private String assignedUserName;

}
