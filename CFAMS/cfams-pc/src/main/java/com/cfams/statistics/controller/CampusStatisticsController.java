package com.cfams.statistics.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.cfams.common.annotation.Log;
import com.cfams.common.core.controller.BaseController;
import com.cfams.common.core.domain.AjaxResult;
import com.cfams.common.enums.BusinessType;
import com.cfams.common.utils.poi.ExcelUtil;
import com.cfams.statistics.domain.dto.StatisticsQueryDTO;
import com.cfams.statistics.domain.vo.StatisticsRowVO;
import com.cfams.statistics.service.CampusStatisticsService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 看板、统计报表及同口径Excel导出。日期参数使用java.util.Date。
 */
@RestController
@RequestMapping("/campus/statistics")
public class CampusStatisticsController extends BaseController {

    @Autowired
    private CampusStatisticsService campusStatisticsService;

    /**
     * 查询当前资产存量、授权待办、期间比率、分布和趋势。
     */
    @GetMapping("/dashboard")
    @PreAuthorize("@ss.hasPermi('campus:dashboard:list')")
    public AjaxResult dashboard(StatisticsQueryDTO query) {
        return success(campusStatisticsService.dashboard(query));
    }

    /**
     * 按日期、业务类型、分组维度及数据权限查询区间事件报表。
     */
    @GetMapping("/reports")
    @PreAuthorize("@ss.hasPermi('campus:reports:list')")
    public AjaxResult reports(StatisticsQueryDTO query) {
        AjaxResult result = success(campusStatisticsService.reports(query));
        result.put("limitations", CampusStatisticsService.LIMITATIONS);
        result.put("currentGrouping", !"MONTH".equals(query.getDimension()));
        String groupingBasis;
        switch (query.getDimension()) {
            case "CATEGORY":
                groupingBasis = "CURRENT_ASSET_CATEGORY";
                break;
            case "DEPARTMENT":
                groupingBasis =
                        "CURRENT_DEPARTMENT_FOR_ASSET; DOCUMENT_DEPARTMENT_SNAPSHOT_FOR_BUSINESS";
                break;
            default:
                groupingBasis = "ACTUAL_EVENT_MONTH";
                break;
        }
        result.put("groupingBasis", groupingBasis);
        return result;
    }

    /**
     * 按报表查询口径导出统计数据并附分组说明。
     */
    @PostMapping("/reports/export")
    @PreAuthorize("@ss.hasPermi('campus:reports:export')")
    @Log(title = "校园统计导出", businessType = BusinessType.EXPORT)
    public void export(StatisticsQueryDTO query, HttpServletResponse response) {
        List<StatisticsRowVO> rows = campusStatisticsService.reports(query);
        String basis;
        switch (query.getDimension()) {
            case "CATEGORY":
                basis = "当前资产分类分组";
                break;
            case "DEPARTMENT":
                basis = "资产当前部门、业务单据部门快照分组";
                break;
            default:
                basis = "实际事件月份分组";
                break;
        }
        new ExcelUtil<>(StatisticsRowVO.class)
                .exportExcel(response, rows, "校园业务统计", basis + "；资产列为区间入册量，非历史存量；费用按最终核验日期");
    }
}
