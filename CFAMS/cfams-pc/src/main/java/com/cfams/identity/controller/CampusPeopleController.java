package com.cfams.identity.controller;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.cfams.common.annotation.Log;
import com.cfams.common.enums.BusinessType;
import com.cfams.common.core.controller.BaseController;
import com.cfams.common.core.domain.AjaxResult;
import com.cfams.common.core.page.TableDataInfo;
import com.cfams.common.exception.ServiceException;
import com.cfams.common.utils.poi.ExcelUtil;
import com.cfams.identity.domain.dto.*;
import com.cfams.identity.service.CampusIdentityService;

/** PC人员名册接口，列表使用若依 pageNum/pageSize。 */
@RestController
@RequestMapping("/campus/people")
public class CampusPeopleController extends BaseController {
    @Autowired
    private CampusIdentityService campusIdentityService;

    /** 分页返回当前用户有权访问的人员名册。 */
    @GetMapping
    @PreAuthorize("@ss.hasPermi('campus:people:list')")
    public TableDataInfo list(PersonQueryDTO query) {
        startPage();
        return getDataTable(campusIdentityService.people(query));
    }
    /** 读取人员详情并验证当前用户的数据访问范围。 */
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('campus:people:list')")
    public AjaxResult detail(@PathVariable Long id) {
        return success(campusIdentityService.detail(id));
    }

    /** 校验人员参数并新增名册记录。 */
    @PostMapping
    @PreAuthorize("@ss.hasPermi('campus:people:add')")
    @Log(title = "校园人员新增", businessType = BusinessType.INSERT)
    public AjaxResult add(@Valid @RequestBody PersonSaveDTO dto) {
        campusIdentityService.save(null, dto);
        return success();
    }
    /** 校验人员参数并修改指定名册记录。 */
    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('campus:people:edit')")
    @Log(title = "校园人员修改", businessType = BusinessType.UPDATE)
    public AjaxResult edit(@PathVariable Long id, @Valid @RequestBody PersonSaveDTO dto) {
        campusIdentityService.save(id, dto);
        return success();
    }
    /** 停用指定人员并撤销尚未使用的绑定凭据。 */
    @PutMapping("/{id}/disable")
    @PreAuthorize("@ss.hasPermi('campus:people:edit')")
    @Log(title = "校园人员停用", businessType = BusinessType.UPDATE)
    public AjaxResult disable(@PathVariable Long id) {
        campusIdentityService.disable(id);
        return success();
    }

    /** 校验上传文件大小后整批导入人员名册。 */
    @PostMapping("/import")
    @PreAuthorize("@ss.hasPermi('campus:people:import')")
    @Log(title = "校园人员导入", businessType = BusinessType.IMPORT, isSaveRequestData = false)
    public AjaxResult importFile(@RequestParam MultipartFile file) throws Exception {
        if (file.isEmpty() || file.getSize() > 10 * 1024 * 1024) throw new ServiceException("请选择10MB以内的Excel文件");
        try (java.io.InputStream input = file.getInputStream()) {
            return success(campusIdentityService.importFile(input));
        }
    }
    /** 输出带人员字段表头的名册导入模板。 */
    @PostMapping("/importTemplate")
    @PreAuthorize("@ss.hasPermi('campus:people:import')")
    public void template(HttpServletResponse response) {
        new ExcelUtil<>(PersonSaveDTO.class).importTemplateExcel(response, "校园人员名册");
    }
}
