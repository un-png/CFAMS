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
import com.cfams.asset.domain.dto.LocationSaveDTO;
import com.cfams.asset.service.CampusAssetService;
import com.cfams.asset.domain.dto.LocationQueryDTO;

/** PC 场所管理，PUT完整表单含status执行启停，数据范围由服务器计算。 */
@RestController
@RequestMapping("/campus/locations")
public class PcLocationController extends BaseController {
    @Autowired
    private CampusAssetService campusAssetService;

    /** 计算数据范围后分页查询场所。 */
    @PreAuthorize("@ss.hasPermi('campus:locations:list')")
    @GetMapping("/list")
    public TableDataInfo list(LocationQueryDTO query) {
        campusAssetService.scope(query);
        startPage();
        return getDataTable(campusAssetService.locations(query));
    }

    /** 查询有权访问的场所详情。 */
    @PreAuthorize("@ss.hasPermi('campus:locations:list')")
    @GetMapping("/{id}")
    public AjaxResult detail(@PathVariable Long id) {
        return success(campusAssetService.location(id));
    }

    /** 校验场所表单后新增场所节点。 */
    @PreAuthorize("@ss.hasPermi('campus:locations:add')")
    @Log(title = "资产场所", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult create(@Valid @RequestBody LocationSaveDTO body) {
        return success(campusAssetService.saveLocation(body, true));
    }

    /** 校验场所表单后更新层级、部门及启停状态。 */
    @PreAuthorize("@ss.hasPermi('campus:locations:edit')")
    @Log(title = "资产场所", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult update(@Valid @RequestBody LocationSaveDTO body) {
        return success(campusAssetService.saveLocation(body, false));
    }
}
