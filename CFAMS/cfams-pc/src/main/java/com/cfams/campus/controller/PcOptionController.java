package com.cfams.campus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.cfams.common.core.controller.BaseController;
import com.cfams.common.core.domain.AjaxResult;
import com.cfams.campus.domain.dto.CampusOptionQueryDTO;
import com.cfams.campus.service.CampusOptionService;

/** 授权业务页面的最小选项投影。写接口仍独立校验所选ID。 */
@RestController
@RequestMapping("/campus/options")
public class PcOptionController extends BaseController {
    @Autowired
    private CampusOptionService campusOptionService;

    /** 查询指定类型的授权选项，供PC业务页面筛选或选择关联记录。 */
    @PreAuthorize("@ss.hasAnyPermi('campus:assets:list,campus:assets:add,campus:assets:edit,campus:locations:list,campus:people:list,campus:binding:list,campus:borrow:list,campus:borrowRecords:list,campus:handover:list,campus:repairs:list,campus:repairVerify:list,campus:repairRecords:list,campus:inventory:list,campus:inventoryReview:list,campus:scrap:list,campus:scrapAudit:list,campus:dashboard:list,campus:reports:list,campus:labels:list,campus:categories:list')")
    @GetMapping("/{type}")
    public AjaxResult options(@PathVariable String type, CampusOptionQueryDTO query) {
        return success(campusOptionService.selectOptions(type, query));
    }
}
