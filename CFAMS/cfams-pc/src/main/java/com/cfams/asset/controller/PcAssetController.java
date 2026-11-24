package com.cfams.asset.controller;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.cfams.common.annotation.Log;
import com.cfams.common.enums.BusinessType;
import com.cfams.common.exception.ServiceException;
import com.cfams.common.core.controller.BaseController;
import com.cfams.common.core.domain.AjaxResult;
import com.cfams.common.core.page.TableDataInfo;
import com.cfams.asset.domain.dto.AssetSaveDTO;
import com.cfams.asset.service.CampusAssetService;
import com.cfams.asset.service.CampusAssetExcelService;
import com.cfams.asset.domain.dto.AssetQueryDTO;

/** PC台账维护，不开放业务状态通用更新或资产删除接口。 */
@RestController
@RequestMapping("/campus/assets")
public class PcAssetController extends BaseController {
    @Autowired
    private CampusAssetService campusAssetService;
    @Autowired
    private CampusAssetExcelService campusAssetExcelService;

    /** 计算数据范围后分页查询资产台账。 */
    @PreAuthorize("@ss.hasPermi('campus:assets:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetQueryDTO query) {
        campusAssetService.scope(query);
        startPage();
        return getDataTable(campusAssetService.assets(query));
    }

    /** 查询有权访问的资产详情。 */
    @PreAuthorize("@ss.hasPermi('campus:assets:query')")
    @GetMapping("/{id}")
    public AjaxResult detail(@PathVariable Long id) {
        return success(campusAssetService.asset(id));
    }

    /** 在开始履历分页前单独校验资产数据范围。 */
    @PreAuthorize("@ss.hasPermi('campus:assets:query')")
    @GetMapping("/{id}/history")
    public TableDataInfo history(@PathVariable Long id) {
        campusAssetService.asset(id);
        startPage();
        return getDataTable(campusAssetService.history(id));
    }

    /** 接收并校验登记表单后新增资产。 */
    @PreAuthorize("@ss.hasPermi('campus:assets:add')")
    @Log(title = "资产登记", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult create(@Valid @RequestBody AssetSaveDTO body) {
        return success(campusAssetService.create(body));
    }

    /** 接收并校验维护表单后更新在库资产台账。 */
    @PreAuthorize("@ss.hasPermi('campus:assets:edit')")
    @Log(title = "资产台账", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult update(@Valid @RequestBody AssetSaveDTO body) {
        return success(campusAssetService.update(body));
    }

    /** multipart字段file，最大10MB和2000行；返回data.total/imported/errors。 */
    @PreAuthorize("@ss.hasPermi('campus:assets:import')")
    @Log(title = "资产整批导入", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public AjaxResult importFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty() || file.getSize() > 10L * 1024 * 1024) {
            throw new ServiceException("请选择不超过10MB的Excel文件");
        }
        try (java.io.InputStream input = file.getInputStream()) {
            return success(campusAssetExcelService.importWorkbook(input));
        }
    }

    /** 设置下载响应并输出资产导入模板。 */
    @PreAuthorize("@ss.hasPermi('campus:assets:import')")
    @GetMapping("/importTemplate")
    public void template(HttpServletResponse response) throws IOException {
        downloadHeaders(response, "asset-template.xlsx");
        campusAssetExcelService.template(response.getOutputStream());
    }

    /** 与列表同一筛选和数据范围，导出不读取客户端分页参数。 */
    @PreAuthorize("@ss.hasPermi('campus:assets:export')")
    @Log(title = "资产明细导出", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(AssetQueryDTO query, HttpServletResponse response) throws IOException {
        campusAssetService.scope(query);
        clearPage();
        java.util.List<com.cfams.asset.domain.vo.AssetVO> values = campusAssetService.assets(query);
        downloadHeaders(response, "assets.xlsx");
        campusAssetExcelService.export(values, response.getOutputStream());
    }

    /** 设置Excel下载的内容类型及附件文件名。 */
    private static void downloadHeaders(HttpServletResponse response, String filename) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
    }
}
