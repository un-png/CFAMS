package com.cfams.attachment.service;

import com.cfams.common.entity.CampusAttachment;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/** 业务服务契约；由对应实现类负责权限、事务和业务处理。 */
public interface CampusAttachmentService {

    /**
     * 校验业务读取权限后查询附件列表。
     *
     * @param type 附件关联业务类型
     * @param id 附件关联业务单据或资产编号
     * @return 符合条件的业务附件列表
     */
    List<CampusAttachment> list(String type, Long id);

    /**
     * 检查附件所属业务的访问权限后生成短期签名地址。
     *
     * @param id 业务附件编号
     * @return 短期有效的附件签名地址
     */
    String url(Long id);

    /**
     * 读取资产并复核上传权限，校验附件内容后写入私有OSS和元数据，事务回滚时清理对象。
     *
     * @param type 附件关联业务类型
     * @param id 附件关联业务单据或资产编号
     * @param file 上传文件
     * @return 附件元数据
     * @throws IOException 读取上传文件内容失败
     */
    CampusAttachment upload(String type, Long id, MultipartFile file) throws IOException;
}
