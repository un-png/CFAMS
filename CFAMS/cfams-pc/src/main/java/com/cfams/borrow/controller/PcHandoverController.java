package com.cfams.borrow.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.cfams.borrow.domain.dto.BorrowQueryDTO;
import com.cfams.borrow.domain.dto.HandoverConfirmDTO;
import com.cfams.borrow.service.CampusBorrowService;
import com.cfams.common.annotation.Log;
import com.cfams.common.core.controller.BaseController;
import com.cfams.common.core.domain.AjaxResult;
import com.cfams.common.core.page.TableDataInfo;
import com.cfams.common.enums.BusinessType;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** PC端业务接口，负责请求绑定和响应封装。 */
@RestController
@RequestMapping("/campus/handover")
public class PcHandoverController extends BaseController {

    @Autowired
    private CampusBorrowService campusBorrowService;

    /**
     * 分页查询当前用户可见的借还交接单列表。
     *
     * @param query 查询条件及数据范围
     * @return 分页记录及总数
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('campus:handover:list')")
    public TableDataInfo list(BorrowQueryDTO query) {
        campusBorrowService.scope(query);
        startPage();
        return getDataTable(campusBorrowService.handovers(query));
    }

    /**
     * 查询借还交接单详情并校验当前用户的数据访问权限。
     *
     * @param id 借还交接单编号
     * @return 统一响应及业务数据
     */
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('campus:handover:query')")
    public AjaxResult detail(@PathVariable Long id) {
        return success(campusBorrowService.handover(id));
    }

    /**
     * 核对人员、凭证及单据状态后确认或驳回交接，同步借用和资产状态。
     *
     * @param id 借还交接单编号
     * @param body 请求参数
     * @return 统一响应及业务数据
     */
    @PutMapping("/{id}/confirm")
    @PreAuthorize("@ss.hasPermi('campus:handover:confirm')")
    @Log(title = "借还交接", businessType = BusinessType.UPDATE)
    public AjaxResult confirm(@PathVariable Long id, @Valid @RequestBody HandoverConfirmDTO body) {
        campusBorrowService.confirm(
                id,
                body.getApprove(),
                body.getConditionStatus(),
                body.getOpinion(),
                body.getVoucherToken());
        return success();
    }
}
