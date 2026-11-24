package com.cfams.statistics.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.cfams.campus.service.CampusAccessService;
import com.cfams.common.exception.ServiceException;
import com.cfams.common.utils.SecurityUtils;
import com.cfams.statistics.domain.dto.StatisticsQueryDTO;
import com.cfams.statistics.domain.vo.*;
import com.cfams.statistics.mapper.CampusStatisticsMapper;
import com.cfams.statistics.service.CampusStatisticsService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 真实统计口径和数据范围校验。
 */
@Service
public class CampusStatisticsServiceImpl implements CampusStatisticsService {

    @Autowired
    private CampusStatisticsMapper campusStatisticsMapper;

    @Autowired
    private CampusAccessService campusAccessService;

    /**
     * 校验统计范围和分组维度，填充默认日期并应用数据权限。
     */
    private void prepare(StatisticsQueryDTO query) {
        if (query.getReportType() == null
                || !Set.of("ALL", "ASSET", "BORROW", "REPAIR", "INVENTORY", "SCRAP")
                        .contains(query.getReportType())) {
            throw new ServiceException("reportType必须为ALL/ASSET/BORROW/REPAIR/INVENTORY/SCRAP");
        }
        if (query.getDimension() == null
                || !Set.of("MONTH", "CATEGORY", "DEPARTMENT").contains(query.getDimension())) {
            throw new ServiceException("dimension必须为MONTH/CATEGORY/DEPARTMENT");
        }
        if (query.getBeginTime() == null && query.getEndTime() == null) {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            query.setBeginTime(
                    Date.from(
                            tomorrow.minusYears(1)
                                    .atStartOfDay(ZoneId.systemDefault())
                                    .toInstant()));
            query.setEndTime(Date.from(tomorrow.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        if (query.getBeginTime() == null
                || query.getEndTime() == null
                || !query.getBeginTime().before(query.getEndTime())) {
            throw new ServiceException("beginTime和endTime须同时提供，结束时间必须晚于开始时间");
        }
        if (query.getEndTime().getTime() - query.getBeginTime().getTime() > 366L * 10 * 86400000) {
            throw new ServiceException("单次统计时间范围不得超过10年");
        }
        if ((query.getMinUseYears() != null && query.getMinUseYears() < 0)
                || (query.getMaxUseYears() != null && query.getMaxUseYears() < 0)
                || (query.getMinUseYears() != null
                        && query.getMaxUseYears() != null
                        && query.getMinUseYears() > query.getMaxUseYears())) {
            throw new ServiceException("使用年限范围不合法");
        }
        campusAccessService.scope(query);
    }

    /**
     * 查询当前资产存量、授权待办、期间比率、分布和趋势。
     */
    @Transactional(readOnly = true)
    @Override
    public DashboardVO dashboard(StatisticsQueryDTO query) {
        query.setDimension("MONTH");
        query.setReportType("ALL");
        prepare(query);
        DashboardVO vo = new DashboardVO();
        vo.setStock(campusStatisticsMapper.stock(query));
        query.setPendingUserId(SecurityUtils.getUserId());
        query.setPendingAdmin(SecurityUtils.isAdmin());
        vo.setPending(campusStatisticsMapper.pending(query));
        vo.setRates(campusStatisticsMapper.rates(query));
        vo.getRates()
                .setBorrowRate(
                        percentage(
                                vo.getRates().getBorrowedAssetCount(),
                                vo.getRates().getPeriodAssetCount()));
        vo.getRates()
                .setRepairRate(
                        percentage(
                                vo.getRates().getRepairedAssetCount(),
                                vo.getRates().getPeriodAssetCount()));
        vo.setCategories(campusStatisticsMapper.categories(query));
        vo.setDepartments(campusStatisticsMapper.departments(query));
        vo.setTrend(campusStatisticsMapper.reports(query));
        vo.setLimitations(LIMITATIONS);
        return vo;
    }

    /**
     * 计算保留两位小数的百分比；分母为零时返回空值。
     */
    private BigDecimal percentage(long numerator, long denominator) {
        return denominator == 0
                ? null
                : BigDecimal.valueOf(numerator)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    /**
     * 按日期、业务类型、分组维度及数据权限查询区间事件报表。
     */
    @Override
    public List<StatisticsRowVO> reports(StatisticsQueryDTO query) {
        prepare(query);
        return campusStatisticsMapper.reports(query);
    }
}
