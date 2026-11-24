package com.cfams.inventory.domain.vo;

import lombok.Getter;
import lombok.Setter;

import com.cfams.common.entity.CaInventoryTask;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * 查询投影：持久化字段继承实体，附加展示字段；保持原JSON字段集合。
 */
@JsonIgnoreProperties({"updateBy"})
@Getter
@Setter
public class InventoryTaskVO extends CaInventoryTask {

    private String deptName;

    private int totalCount;

    private int checkedCount;

    private int reviewedCount;

    private List<Long> userIds;

}
