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
 * PC明细查询与差异复核，不接受现场扫码结果写入。
 */
@RestController
@RequestMapping("/campus/inventoryReview")
public class CampusInventoryReviewController extends BaseController {

    @Autowired
    private CampusInventoryService campusInventoryService;

    /**
     * 分页查询当前用户可访问的盘点明细。
     */
    @PreAuthorize("@ss.hasAnyPermi('campus:inventoryReview:list,campus:inventory:query')")
    @GetMapping("/list")
    public TableDataInfo list(InventoryItemQueryDTO query) {
        startPage();
        return getDataTable(campusInventoryService.items(query));
    }

    /**
     * 查询单条盘点明细及其所属任务的访问权限。
     */
    @PreAuthorize("@ss.hasAnyPermi('campus:inventoryReview:query,campus:inventory:query')")
    @GetMapping("/{id}")
    public AjaxResult detail(@PathVariable Long id) {
        return success(campusInventoryService.item(id));
    }

    /**
     * 复核盘点差异，按结论退回重盘或更新台账及维修关联。
     */
    @PreAuthorize("@ss.hasPermi('campus:inventoryReview:review')")
    @Log(title = "盘点差异复核", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/review")
    public AjaxResult review(
            @PathVariable Long id, @Valid @RequestBody InventoryReviewDTO request) {
        campusInventoryService.review(id, request);
        return success();
    }
}
