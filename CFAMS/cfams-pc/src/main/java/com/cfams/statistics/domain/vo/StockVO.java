package com.cfams.statistics.domain.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 当前未注销资产，不用事件日期过滤当前存量。
 */
@Getter
@Setter
public class StockVO {

    private long assetCount;

    private BigDecimal registeredValue;

    private long borrowedCount;

    private long repairingCount;

}
