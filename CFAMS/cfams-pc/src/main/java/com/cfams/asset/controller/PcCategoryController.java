package com.cfams.asset.controller;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.cfams.common.annotation.Log;
import com.cfams.common.enums.BusinessType;
import com.cfams.common.core.controller.BaseController;
import com.cfams.common.core.domain.AjaxResult;
import com.cfams.common.core.page.TableDataInfo;
import com.cfams.asset.domain.dto.CategorySaveDTO;
import com.cfams.asset.service.CampusAssetService;
import com.cfams.asset.domain.dto.CategoryQueryDTO;

/** PC 类别管理：根parentId为空；PUT完整表单含status执行启停。 */
@RestController
@RequestMapping("/campus/categories")
public class PcCategoryController extends BaseController {
    @Autowired
    private CampusAssetService campusAssetService;

    /** keyword/status过滤，若依pageNum/pageSize分页，rows内保留parentId。 */
    @PreAuthorize("@ss.hasPermi('campus:categories:list')")
    @GetMapping("/list")
    public TableDataInfo list(CategoryQueryDTO query) {
        startPage();
        return getDataTable(campusAssetService.categories(query));
    }

    /** 查询指定类别详情。 */
    @PreAuthorize("@ss.hasPermi('campus:categories:list')")
    @GetMapping("/{id}")
    public AjaxResult detail(@PathVariable Long id) {
        return success(campusAssetService.category(id));
    }

    /** 校验类别表单后新增类别节点。 */
    @PreAuthorize("@ss.hasPermi('campus:categories:add')")
    @Log(title = "资产类别", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult create(@Valid @RequestBody CategorySaveDTO body) {
        return success(campusAssetService.saveCategory(body, true));
    }

    /** 校验类别表单后更新节点及启停状态。 */
    @PreAuthorize("@ss.hasPermi('campus:categories:edit')")
    @Log(title = "资产类别", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult update(@Valid @RequestBody CategorySaveDTO body) {
        return success(campusAssetService.saveCategory(body, false));
    }
}
