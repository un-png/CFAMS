package com.cfams.statistics.domain.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 看板由当前存量和区间事件两个不同口径组成。
 */
@Getter
@Setter
public class DashboardVO {

    private final boolean currentGrouping = true;

    private final String groupingBasis = "CURRENT_STOCK_DISTRIBUTION; EVENT_TIME_MONTHLY_TREND";

    private StockVO stock;

    private PendingVO pending;

    private RateVO rates;

    private List<DistributionVO> categories;

    private List<DistributionVO> departments;

    private List<StatisticsRowVO> trend;

    private String limitations;

}
