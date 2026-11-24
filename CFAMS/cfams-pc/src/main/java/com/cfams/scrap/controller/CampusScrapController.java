package com.cfams.scrap.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.cfams.common.annotation.Log;
import com.cfams.common.core.controller.BaseController;
import com.cfams.common.core.domain.AjaxResult;
import com.cfams.common.core.page.TableDataInfo;
import com.cfams.common.enums.BusinessType;
import com.cfams.scrap.domain.dto.*;
import com.cfams.scrap.domain.vo.*;
import com.cfams.scrap.service.CampusScrapService;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * PC处置申请及固定两级审批。
 */
@RestController
@RequestMapping("/campus/scrap")
public class CampusScrapController extends BaseController {

    @Autowired
    private CampusScrapService campusScrapService;

    /**
     * 按查询条件和数据权限查询处置单列表。
     */
    @PreAuthorize("@ss.hasAnyPermi('campus:scrap:list,campus:scrapAudit:list')")
    @GetMapping("/list")
    public TableDataInfo list(ScrapQueryDTO query) {
        startPage();
        return getDataTable(campusScrapService.list(query));
    }

    /**
     * 查询处置详情、审核记录及证明附件并校验访问权限。
     */
    @PreAuthorize("@ss.hasAnyPermi('campus:scrap:query,campus:scrapAudit:query')")
    @GetMapping("/{id}")
    public AjaxResult detail(@PathVariable Long id) {
        return success(campusScrapService.detail(id));
    }

    /**
     * 校验资产及未完成业务后创建处置申请。
     */
    @PreAuthorize("@ss.hasPermi('campus:scrap:add')")
    @Log(title = "报废申请", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult create(@Valid @RequestBody ScrapCreateDTO request) {
        return success(campusScrapService.create(request));
    }

    /**
     * 办理处置初审并返回统一响应。
     */
    @PreAuthorize("@ss.hasPermi('campus:scrapAudit:firstAudit')")
    @Log(title = "报废初审", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/firstAudit")
    public AjaxResult firstAudit(@PathVariable Long id, @Valid @RequestBody ScrapAuditDTO request) {
        campusScrapService.audit(id, "1", request);
        return success();
    }

    /**
     * 办理处置复审，审核通过后完成资产注销。
     */
    @PreAuthorize("@ss.hasPermi('campus:scrapAudit:finalAudit')")
    @Log(title = "报废复审注销", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/finalAudit")
    public AjaxResult finalAudit(@PathVariable Long id, @Valid @RequestBody ScrapAuditDTO request) {
        campusScrapService.audit(id, "2", request);
        return success();
    }
}
