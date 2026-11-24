package com.cfams.identity.controller;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.cfams.common.annotation.Log;
import com.cfams.common.enums.BusinessType;
import com.cfams.common.core.controller.BaseController;
import com.cfams.common.core.domain.AjaxResult;
import com.cfams.common.core.page.TableDataInfo;
import com.cfams.identity.domain.dto.*;
import com.cfams.identity.service.CampusIdentityService;

/** PC绑定管理；不提供设置微信ID的接口。 */
@RestController
@RequestMapping("/campus/binding")
public class CampusBindingController extends BaseController {
    @Autowired
    private CampusIdentityService campusIdentityService;

    /** 分页查询人员绑定管理信息。 */
    @GetMapping
    @PreAuthorize("@ss.hasPermi('campus:binding:list')")
    public TableDataInfo list(BindingQueryDTO query) {
        startPage();
        return getDataTable(campusIdentityService.bindings(query));
    }
    /** 发放一次性绑定凭据并禁止浏览器缓存明文响应。 */
    @PostMapping("/{personId}/issue")
    @PreAuthorize("@ss.hasPermi('campus:binding:issue')")
    @Log(title = "发放绑定凭据", businessType = BusinessType.UPDATE, isSaveRequestData = false, isSaveResponseData = false)
    public AjaxResult issue(@PathVariable Long personId, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        return success(campusIdentityService.issue(personId));
    }
    /** 接收线下核验信息并解除人员指定旧绑定。 */
    @PostMapping("/{personId}/rebind")
    @PreAuthorize("@ss.hasPermi('campus:binding:rebind')")
    @Log(title = "核验解除旧绑定", businessType = BusinessType.UPDATE, isSaveResponseData = false)
    public AjaxResult rebind(@PathVariable Long personId, @Valid @RequestBody BindingRebindDTO dto) {
        campusIdentityService.rebind(personId, dto);
        return success();
    }
}
