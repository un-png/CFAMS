package com.cfams.statistics.service;

import com.cfams.statistics.domain.dto.StatisticsQueryDTO;
import com.cfams.statistics.domain.vo.DashboardVO;
import com.cfams.statistics.domain.vo.StatisticsRowVO;

import java.util.List;

/**
 * 校园看板与报表统计服务，统一统计口径与数据范围。
 */
public interface CampusStatisticsService {

    public static final String LIMITATIONS =
            "存量、分类和部门分布为当前未注销台账，不受事件日期筛选；"
                    + "区间报表资产列为入册量，借用按actual_pickup_time，报修按accept_time且排除驳回，"
                    + "维修费用仅计status=4且verify_time非空的最终费用，注销按completed_time。"
                    + "分类、位置和使用年限使用当前台账；借用、维修、报废部门使用单据快照，盘点使用任务部门。"
                    + "不提供历史时点资产存量或历史分类分布；使用年限按采购日期截至查询当天的完整年数，未知日期不匹配年限条件。"
                    + "盘点异常按现场疑似丢失、完好异常或与发布快照的位置差异计数，不等于最终复核结论；"
                    + "比率分母为期间曾在册的去重资产，分子为同一资产集合在期间确认领取或受理未驳回报修的去重资产，单位百分比；分母为0时比率为null。"
                    + "比率资产范围仍按当前台账部门、类别及责任人筛选，不冒充历史组织分布。"
                    + "待办同时按具体操作权限、对应角色数据范围和看板数据范围过滤，盘点待办只支持部门筛选。";

    /**
     * 查询当前资产存量、授权待办、期间比率、分布和趋势。
     */
    DashboardVO dashboard(StatisticsQueryDTO query);

    /**
     * 按日期、业务类型、分组维度及数据权限查询区间事件报表。
     */
    List<StatisticsRowVO> reports(StatisticsQueryDTO query);
}
