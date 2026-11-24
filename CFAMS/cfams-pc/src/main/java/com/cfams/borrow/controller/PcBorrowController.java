package com.cfams.borrow.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.cfams.borrow.domain.dto.*;
import com.cfams.borrow.domain.dto.BorrowQueryDTO;
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
@RequestMapping("/campus/borrow")
public class PcBorrowController extends BaseController {

    @Autowired
    private CampusBorrowService campusBorrowService;

    /**
     * 分页查询当前用户可见的借用单列表。
     *
     * @param query 查询条件及数据范围
     * @return 分页记录及总数
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('campus:borrow:list')")
    public TableDataInfo list(BorrowQueryDTO query) {
        campusBorrowService.scope(query);
        startPage();
        return getDataTable(campusBorrowService.list(query));
    }

    /**
     * 在分页前发送到期提醒，再分页查询借用记录。
     *
     * @param query 查询条件及数据范围
     * @return 分页记录及总数
     */
    @GetMapping("/records")
    @PreAuthorize("@ss.hasPermi('campus:borrowRecords:list')")
    public TableDataInfo records(BorrowQueryDTO query) {
        campusBorrowService.scope(query);
        campusBorrowService.remind(query);
        startPage();
        return getDataTable(campusBorrowService.list(query));
    }

    /**
     * 查询借用单详情并校验当前用户的数据访问权限。
     *
     * @param id 借用单编号
     * @return 统一响应及业务数据
     */
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasAnyPermi('campus:borrow:query,campus:borrowRecords:query')")
    public AjaxResult detail(@PathVariable Long id) {
        return success(campusBorrowService.detail(id));
    }

    /**
     * 校验审核权限与预约冲突后审核借用申请，同步凭证、历史及通知。
     *
     * @param id 借用单编号
     * @param body 请求参数
     * @return 统一响应及业务数据
     */
    @PutMapping("/{id}/audit")
    @PreAuthorize("@ss.hasPermi('campus:borrow:audit')")
    @Log(title = "借用审核", businessType = BusinessType.UPDATE)
    public AjaxResult audit(@PathVariable Long id, @Valid @RequestBody BorrowAuditDTO body) {
        campusBorrowService.audit(id, body.getApprove(), body.getOpinion());
        return success();
    }

    /**
     * 取消尚未交付的预约并关闭待处理交接，记录历史并通知申请人。
     *
     * @param id 借用单编号
     * @param body 请求参数
     * @return 统一响应及业务数据
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("@ss.hasPermi('campus:borrow:audit')")
    @Log(title = "取消预约", businessType = BusinessType.UPDATE)
    public AjaxResult cancel(@PathVariable Long id, @Valid @RequestBody BorrowCancelDTO body) {
        campusBorrowService.cancel(id, body.getReason());
        return success();
    }
}
