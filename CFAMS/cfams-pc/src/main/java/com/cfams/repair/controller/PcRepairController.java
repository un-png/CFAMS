package com.cfams.repair.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.cfams.common.annotation.Log;
import com.cfams.common.core.controller.BaseController;
import com.cfams.common.core.domain.AjaxResult;
import com.cfams.common.core.page.TableDataInfo;
import com.cfams.common.enums.BusinessType;
import com.cfams.repair.domain.dto.RepairAssignDTO;
import com.cfams.repair.domain.dto.RepairQueryDTO;
import com.cfams.repair.domain.dto.RepairVerifyDTO;
import com.cfams.repair.service.CampusRepairService;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** PC端业务接口，负责请求绑定和响应封装。 */
@RestController
@RequestMapping("/campus/repairs")
public class PcRepairController extends BaseController {

    @Autowired
    private CampusRepairService campusRepairService;

    /**
     * 分页查询当前用户可见的报修单列表。
     *
     * @param query 查询条件及数据范围
     * @return 分页记录及总数
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('campus:repairs:list')")
    public TableDataInfo list(RepairQueryDTO query) {
        campusRepairService.scope(query);
        startPage();
        return getDataTable(campusRepairService.list(query));
    }

    /**
     * 分页查询维修核验列表，未指定状态时默认查询待核验任务。
     *
     * @param query 查询条件及数据范围
     * @return 分页记录及总数
     */
    @GetMapping("/verification")
    @PreAuthorize("@ss.hasPermi('campus:repairVerify:list')")
    public TableDataInfo verification(RepairQueryDTO query) {
        if (query.getStatus() == null) {
            query.setStatus("3");
        }
        campusRepairService.scope(query);
        startPage();
        return getDataTable(campusRepairService.list(query));
    }

    /**
     * 分页查询当前用户可见的维修记录。
     *
     * @param query 查询条件及数据范围
     * @return 分页记录及总数
     */
    @GetMapping("/records")
    @PreAuthorize("@ss.hasPermi('campus:repairRecords:list')")
    public TableDataInfo records(RepairQueryDTO query) {
        campusRepairService.scope(query);
        startPage();
        return getDataTable(campusRepairService.list(query));
    }

    /**
     * 查询报修单详情并校验当前用户的数据访问权限。
     *
     * @param id 报修单编号
     * @return 统一响应及业务数据
     */
    @GetMapping("/{id}")
    @PreAuthorize(
            "@ss.hasAnyPermi('campus:repairs:query,campus:repairVerify:query,campus:repairRecords:query')")
    public AjaxResult detail(@PathVariable Long id) {
        return success(campusRepairService.detail(id));
    }

    /**
     * 查询维修单的过程记录。
     *
     * @param id 报修单编号
     * @return 统一响应及业务数据
     */
    @GetMapping("/{id}/records")
    @PreAuthorize(
            "@ss.hasAnyPermi('campus:repairs:query,campus:repairVerify:query,campus:repairRecords:query')")
    public AjaxResult process(@PathVariable Long id) {
        return success(campusRepairService.records(id));
    }

    /**
     * 校验权限、任务状态和维修执行资格后受理、驳回或改派，联动资产及通知。
     *
     * @param id 报修单编号
     * @param body 请求参数
     * @return 统一响应及业务数据
     */
    @PutMapping("/{id}/assign")
    @PreAuthorize("@ss.hasPermi('campus:repairs:assign')")
    @Log(title = "报修受理派工", businessType = BusinessType.UPDATE)
    public AjaxResult assign(@PathVariable Long id, @Valid @RequestBody RepairAssignDTO body) {
        campusRepairService.assign(id, body.getApprove(), body.getRepairerUserId(), body.getOpinion(), false);
        return success();
    }

    /**
     * 接收维修改派请求并调用业务服务。
     *
     * @param id 报修单编号
     * @param body 请求参数
     * @return 统一响应及业务数据
     */
    @PutMapping("/{id}/reassign")
    @PreAuthorize("@ss.hasPermi('campus:repairs:assign')")
    @Log(title = "维修改派", businessType = BusinessType.UPDATE)
    public AjaxResult reassign(@PathVariable Long id, @Valid @RequestBody RepairAssignDTO body) {
        campusRepairService.assign(id, body.getApprove(), body.getRepairerUserId(), body.getOpinion(), true);
        return success();
    }

    /**
     * 核验维修反馈并联动资产状态、过程记录及相关人员通知。
     *
     * @param id 报修单编号
     * @param body 请求参数
     * @return 统一响应及业务数据
     */
    @PutMapping("/{id}/verify")
    @PreAuthorize("@ss.hasPermi('campus:repairVerify:verify')")
    @Log(title = "维修结果核验", businessType = BusinessType.UPDATE)
    public AjaxResult verify(@PathVariable Long id, @Valid @RequestBody RepairVerifyDTO body) {
        campusRepairService.verify(id, body.getApprove(), body.getOpinion());
        return success();
    }
}
