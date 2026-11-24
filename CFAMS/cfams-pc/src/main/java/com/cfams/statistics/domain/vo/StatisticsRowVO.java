package com.cfams.statistics.domain.vo;

import lombok.Getter;
import lombok.Setter;

import com.cfams.common.annotation.Excel;

import java.math.BigDecimal;

/**
 * 数量为事件数，金额为decimal；资产列表示区间入册量，不冒充历史存量。
 */
@Getter
@Setter
public class StatisticsRowVO {

    @Excel(name = "统计维度")
    private String dimension;

    @Excel(name = "入册数量")
    private Long assetCount;

    @Excel(name = "入册登记价值")
    private BigDecimal registeredValue;

    @Excel(name = "确认借出数量")
    private Long borrowCount;

    @Excel(name = "受理报修数量")
    private Long repairCount;

    @Excel(name = "核验维修费用")
    private BigDecimal repairCost;

    @Excel(name = "已盘明细数")
    private Long inventoryCount;

    @Excel(name = "现场异常明细数")
    private Long inventoryDifferenceCount;

    @Excel(name = "注销数量")
    private Long scrapCount;

}
