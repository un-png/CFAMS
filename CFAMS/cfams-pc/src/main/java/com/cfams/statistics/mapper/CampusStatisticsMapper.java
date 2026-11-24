package com.cfams.statistics.mapper;

import com.cfams.statistics.domain.dto.StatisticsQueryDTO;
import com.cfams.statistics.domain.vo.*;

import java.util.List;

/**
 * 统计仅聚合真实业务表。
 */
public interface CampusStatisticsMapper {

    /**
     * 按授权范围汇总当前未注销资产存量。
     */
    StockVO stock(StatisticsQueryDTO query);

    /**
     * 按具体操作权限及数据范围统计各类待办。
     */
    PendingVO pending(StatisticsQueryDTO query);

    /**
     * 统计期间曾在册资产及借用、报修去重资产数量。
     */
    RateVO rates(StatisticsQueryDTO query);

    /**
     * 按当前资产类别统计未注销资产分布。
     */
    List<DistributionVO> categories(StatisticsQueryDTO query);

    /**
     * 按当前资产部门统计未注销资产分布。
     */
    List<DistributionVO> departments(StatisticsQueryDTO query);

    /**
     * 按日期、业务类型、分组维度及数据权限查询区间事件报表。
     */
    List<StatisticsRowVO> reports(StatisticsQueryDTO query);
}
