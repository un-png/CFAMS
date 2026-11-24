package com.cfams.attachment.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.cfams.attachment.service.CampusAttachmentService;
import com.cfams.common.core.controller.BaseController;
import com.cfams.common.core.domain.AjaxResult;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/** PC端业务接口，负责请求绑定和响应封装。 */
@RestController
@RequestMapping("/campus/attachments")
public class PcAttachmentController extends BaseController {

    @Autowired
    private CampusAttachmentService campusAttachmentService;

    /**
     * 查询指定业务的附件列表。
     *
     * @param bizType 关联业务类型
     * @param bizId 关联业务编号
     * @return 统一响应及业务数据
     */
    @GetMapping("/list")
    public AjaxResult list(@RequestParam String bizType, @RequestParam Long bizId) {
        return success(campusAttachmentService.list(bizType, bizId));
    }

    /**
     * 检查附件所属业务的访问权限后生成短期签名地址。
     *
     * @param id 业务附件编号
     * @return 短期有效的附件签名地址
     */
    @GetMapping("/{id}/url")
    public AjaxResult url(@PathVariable Long id) {
        return success((Object) campusAttachmentService.url(id));
    }

    /**
     * 接收业务附件上传请求并返回附件元数据。
     *
     * @param bizType 关联业务类型
     * @param bizId 关联业务编号
     * @param file 上传文件
     * @return 附件元数据
     * @throws IOException 读取上传文件内容失败
     */
    @PostMapping(consumes = "multipart/form-data")
    public AjaxResult upload(
            @RequestParam String bizType,
            @RequestParam Long bizId,
            @RequestParam MultipartFile file)
            throws IOException {
        return success(campusAttachmentService.upload(bizType, bizId, file));
    }
}
