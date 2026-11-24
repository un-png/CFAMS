package com.cfams.statistics.domain.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 期间曾在册资产为分母，期间发生相应事件的去重资产为分子。
 */
@Getter
@Setter
public class RateVO {

    private long periodAssetCount;

    private long borrowedAssetCount;

    private long repairedAssetCount;

    private BigDecimal borrowRate;

    private BigDecimal repairRate;

    private final String unit = "PERCENT";

}
