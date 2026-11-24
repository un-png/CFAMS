package com.cfams.inventory.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.cfams.common.annotation.Log;
import com.cfams.common.core.controller.BaseController;
import com.cfams.common.core.domain.AjaxResult;
import com.cfams.common.core.page.TableDataInfo;
import com.cfams.common.enums.BusinessType;
import com.cfams.inventory.domain.dto.*;
import com.cfams.inventory.domain.vo.*;
import com.cfams.inventory.service.CampusInventoryService;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * PC任务列表、草稿、发布与执行阶段管理。
 */
@RestController
@RequestMapping("/campus/inventory")
public class CampusInventoryController extends BaseController {

    @Autowired
    private CampusInventoryService campusInventoryService;

    /**
     * 按查询条件和数据权限查询盘点任务列表。
     */
    @PreAuthorize("@ss.hasPermi('campus:inventory:list')")
    @GetMapping("/list")
    public TableDataInfo list(InventoryTaskQueryDTO query) {
        startPage();
        return getDataTable(campusInventoryService.list(query));
    }

    /**
     * 查询盘点任务详情，并校验当前用户的数据访问范围。
     */
    @PreAuthorize("@ss.hasPermi('campus:inventory:query')")
    @GetMapping("/{id}")
    public AjaxResult detail(@PathVariable Long id) {
        return success(campusInventoryService.detail(id));
    }

    /**
     * 校验草稿范围与执行人后创建盘点任务。
     */
    @PreAuthorize("@ss.hasPermi('campus:inventory:add')")
    @Log(title = "盘点任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult create(@Valid @RequestBody InventoryTaskSaveDTO request) {
        return success(campusInventoryService.create(request));
    }

    /**
     * 修改尚未发布的任务草稿及执行人分配。
     */
    @PreAuthorize("@ss.hasPermi('campus:inventory:add')")
    @Log(title = "盘点草稿", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult edit(
            @PathVariable Long id, @Valid @RequestBody InventoryTaskSaveDTO request) {
        campusInventoryService.edit(id, request);
        return success();
    }

    /**
     * 查询草稿范围内可分配执行人的资产清单。
     */
    @PreAuthorize("@ss.hasPermi('campus:inventory:publish')")
    @GetMapping("/{id}/candidates")
    public AjaxResult candidates(@PathVariable Long id) {
        return success(campusInventoryService.candidates(id));
    }

    /**
     * 校验完整分配清单并按资产顺序查询，发布任务并冻结盘点快照。
     */
    @PreAuthorize("@ss.hasPermi('campus:inventory:publish')")
    @Log(title = "盘点发布", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/publish")
    public AjaxResult publish(
            @PathVariable Long id, @Valid @RequestBody InventoryPublishDTO request) {
        campusInventoryService.publish(id, request);
        return success();
    }

    /**
     * 校验计划开始时间后将已发布任务转入执行中。
     */
    @PreAuthorize("@ss.hasPermi('campus:inventory:publish')")
    @Log(title = "盘点开始", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/start")
    public AjaxResult start(@PathVariable Long id) {
        campusInventoryService.start(id);
        return success();
    }

    /**
     * 将执行中的盘点任务提交复核。
     */
    @PreAuthorize("@ss.hasPermi('campus:inventory:publish')")
    @Log(title = "盘点提交复核", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/submit")
    public AjaxResult submit(@PathVariable Long id) {
        campusInventoryService.submit(id);
        return success();
    }

    /**
     * 确认全部明细有复核结论后结项并通知发布人。
     */
    @PreAuthorize("@ss.hasPermi('campus:inventoryReview:review')")
    @Log(title = "盘点结项", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/complete")
    public AjaxResult complete(@PathVariable Long id) {
        campusInventoryService.complete(id);
        return success();
    }
}
