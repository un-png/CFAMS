package com.cfams.message.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.cfams.common.core.controller.BaseController;
import com.cfams.common.core.domain.AjaxResult;
import com.cfams.common.core.page.TableDataInfo;
import com.cfams.message.domain.dto.MessageQueryDTO;
import com.cfams.message.service.CampusMessageService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** PC端业务接口，负责请求绑定和响应封装。 */
@RestController
@RequestMapping("/campus/messages")
public class PcMessageController extends BaseController {

    @Autowired
    private CampusMessageService campusMessageService;

    /**
     * 分页查询当前用户可见的当前用户消息列表。
     *
     * @param query 查询条件及数据范围
     * @return 分页记录及总数
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('campus:messages:list')")
    public TableDataInfo list(MessageQueryDTO query) {
        startPage();
        return getDataTable(campusMessageService.list(query));
    }

    /**
     * 查询当前用户消息详情并校验当前用户的数据访问权限。
     *
     * @param id 当前用户消息编号
     * @return 统一响应及业务数据
     */
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('campus:messages:query')")
    public AjaxResult detail(@PathVariable Long id) {
        return success(campusMessageService.detail(id));
    }

    /**
     * 校验并去重1至100条消息编号，仅将全部属于当前用户的消息标记为已读。
     *
     * @param ids 待处理消息编号集合
     * @return 统一响应及业务数据
     */
    @PutMapping("/read")
    @PreAuthorize("@ss.hasPermi('campus:messages:read')")
    public AjaxResult read(@RequestBody List<Long> ids) {
        campusMessageService.read(ids);
        return success();
    }
}
