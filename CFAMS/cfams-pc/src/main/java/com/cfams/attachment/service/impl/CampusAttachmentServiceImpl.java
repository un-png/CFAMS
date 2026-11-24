package com.cfams.attachment.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.cfams.attachment.domain.vo.AttachmentTargetVO;
import com.cfams.attachment.mapper.CampusAttachmentMapper;
import com.cfams.attachment.service.CampusAttachmentService;
import com.cfams.attachment.service.CampusOssStorage;
import com.cfams.campus.service.CampusAccessService;
import com.cfams.asset.service.CampusAssetStateService;
import com.cfams.common.entity.CampusAttachment;
import com.cfams.common.exception.ServiceException;
import com.cfams.common.utils.SecurityUtils;
import com.cfams.system.service.ISysConfigService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.imageio.ImageIO;

/** 业务服务实现，保留原有业务校验及状态处理规则。 */
@Service
public class CampusAttachmentServiceImpl implements CampusAttachmentService {

    private static final Logger LOG = LoggerFactory.getLogger(CampusAttachmentServiceImpl.class);

    @Autowired
    private CampusAttachmentMapper campusAttachmentMapper;

    @Autowired
    private CampusAccessService campusAccessService;

    @Autowired
    private CampusAssetStateService campusAssetStateService;

    @Autowired
    private CampusOssStorage campusOssStorage;

    @Autowired
    private ISysConfigService iSysConfigService;

    /**
     * 校验业务读取权限后查询附件列表。
     *
     * @param type 附件关联业务类型
     * @param id 附件关联业务单据或资产编号
     * @return 符合条件的业务附件列表
     */
    @Override
    public List<CampusAttachment> list(String type, Long id) {
        authorize(type, id, false);
        return campusAttachmentMapper.list(type, id);
    }

    /**
     * 检查附件所属业务的访问权限后生成短期签名地址。
     *
     * @param id 业务附件编号
     * @return 短期有效的附件签名地址
     */
    @Override
    public String url(Long id) {
        CampusAttachment attachment = campusAttachmentMapper.detail(id);
        if (attachment == null) {
            throw new ServiceException("附件不存在");
        }
        authorize(attachment.getBizType(), attachment.getBizId(), false);
        return campusOssStorage.signedUrl(attachment.getBucketName(), attachment.getObjectKey());
    }

    /**
     * 读取资产并复核上传权限，校验附件内容后写入私有OSS和元数据，事务回滚时清理对象。
     *
     * @param type 附件关联业务类型
     * @param id 附件关联业务单据或资产编号
     * @param file 上传文件
     * @return 附件元数据
     * @throws IOException 读取上传文件内容失败
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public CampusAttachment upload(String type, Long id, MultipartFile file) throws IOException {
        AttachmentTargetVO target = authorize(type, id, true);
        campusAssetStateService.selectForChange(target.getAssetId());
        target = authorize(type, id, true);
        if (campusAttachmentMapper.count(type, id) >= limit("campus.upload.maxCount", 9, 20)) {
            throw new ServiceException("附件数量已达上限");
        }
        if (file == null
                || file.isEmpty()
                || file.getSize() > limit("campus.upload.maxSizeMb", 10, 20) * 1024L * 1024L) {
            throw new ServiceException("附件为空或超过允许大小");
        }
        String name = file.getOriginalFilename();
        if (name == null || name.length() > 255 || name.contains("/") || name.contains("\\")) {
            throw new ServiceException("附件文件名无效");
        }
        String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        String allowed = iSysConfigService.selectConfigByKey("campus.upload.allowedExtensions");
        if (!allowed.isBlank()
                && !Arrays.asList(allowed.toLowerCase(Locale.ROOT).split(",")).contains(ext)) {
            throw new ServiceException("此文件扩展名不在配置允许范围内");
        }
        byte[] bytes = file.getBytes();
        String contentType;
        if (Arrays.asList("png", "jpg", "jpeg").contains(ext)) {
            if (ImageIO.read(new ByteArrayInputStream(bytes)) == null) {
                throw new ServiceException("无法识别的图片内容");
            }
            contentType = "png".equals(ext) ? "image/png" : "image/jpeg";
        } else if ("pdf".equals(ext)
                && "SCRAP".equals(type)
                && bytes.length > 5
                && new String(bytes, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)
                        .equals("%PDF-")) {
            contentType = "application/pdf";
        } else {
            throw new ServiceException("仅支持有效图片，报废证明还可使用PDF");
        }
        String bucket = campusOssStorage.bucket();
        String key =
                "campus/" + type.toLowerCase(Locale.ROOT) + "/" + UUID.randomUUID() + "." + ext;
        campusOssStorage.put(key, bytes, contentType);
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    /**
                     * 业务事务未提交时尝试删除已上传的OSS对象，清理失败仅记录提示。
                     *
                     * @param status 事务完成状态
                     */
                    @Override
                    public void afterCompletion(int status) {
                        if (status != STATUS_COMMITTED) {
                            try {
                                campusOssStorage.delete(key);
                            } catch (RuntimeException error) {
                                LOG.warn("业务事务回滚后的OSS对象清理失败，请核查孤立对象");
                            }
                        }
                    }
                });
        campusAttachmentMapper.insert(
                type, id, name, bucket, key, contentType, bytes.length, SecurityUtils.getUserId());
        return campusAttachmentMapper.byKey(bucket, key);
    }

    /**
     * 读取整数配置并限制到有效范围，格式错误时使用默认值。
     *
     * @param key 系统配置键
     * @param fallback 配置无效时的默认值
     * @param max 允许的最大配置值
     * @return 有效范围内的配置值
     */
    private int limit(String key, int fallback, int max) {
        try {
            return Math.min(max, Math.max(1, Integer.parseInt(iSysConfigService.selectConfigByKey(key))));
        } catch (NumberFormatException error) {
            return fallback;
        }
    }

    /**
     * 根据业务类型、数据范围及当前状态校验附件读取或上传权限。
     *
     * @param type 附件关联业务类型
     * @param id 附件关联业务单据或资产编号
     * @param write 是否进行上传写入
     * @return 已通过权限校验的业务归属投影
     */
    private AttachmentTargetVO authorize(String type, Long id, boolean write) {
        if (id == null || id < 1) {
            throw new ServiceException("业务ID无效");
        }
        String permission;
        switch (type) {
            case "ASSET":
                permission = write ? "campus:assets:edit" : "campus:assets:query";
                break;
            case "SCRAP":
                permission = write ? "campus:scrap:add" : "campus:scrap:query";
                break;
            case "REPAIR":
                permission = "campus:repairs:query";
                break;
            case "REPAIR_RECORD":
                permission = "campus:repairRecords:query";
                break;
            case "INVENTORY_ITEM":
                permission = "campus:inventoryReview:query";
                break;
            default:
                throw new ServiceException("不支持的附件业务类型");
        }
        if (!write && "SCRAP".equals(type)) {
            campusAccessService.requireAnyPermission(permission, "campus:scrapAudit:query");
        } else if (!write && "REPAIR".equals(type)) {
            campusAccessService.requireAnyPermission(
                    permission, "campus:repairVerify:query", "campus:repairRecords:query");
        } else {
            campusAccessService.requirePermission(permission);
        }
        AttachmentTargetVO target = campusAttachmentMapper.target(type, id);
        if (target == null) {
            throw new ServiceException("附件所属业务不存在");
        }
        campusAccessService.requireData(target.getDeptId(), target.getOwnerId());
        if (write && !Arrays.asList("ASSET", "SCRAP").contains(type)) {
            throw new ServiceException("PC端不能替代现场人员上传原始任务证据");
        }
        if (write && "ASSET".equals(type) && "3".equals(target.getStatus())) {
            throw new ServiceException("报废资产附件不能修改");
        }
        if (write
                && "SCRAP".equals(type)
                && (!"0".equals(target.getStatus())
                        || !SecurityUtils.getUserId().equals(target.getOwnerId()))) {
            throw new ServiceException("仅申请人可在初审前补充证明材料");
        }
        return target;
    }
}
