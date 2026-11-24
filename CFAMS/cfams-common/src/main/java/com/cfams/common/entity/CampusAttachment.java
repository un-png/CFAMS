package com.cfams.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** 附件元数据，OSS签名地址按需生成。 */
@Getter
@Setter
public class CampusAttachment {
    /** 附件主键 */
    private Long attachmentId;

    /** 所属业务类型 */
    private String bizType;

    /** 所属业务主键 */
    private Long bizId;

    /** 上传文件名称 */
    private String fileName;

    /** OSS存储桶名称 */
    private String bucketName;

    /** OSS对象键 */
    private String objectKey;

    /** 文件媒体类型 */
    private String contentType;

    /** 文件字节数 */
    private Long fileSize;

    /** 附件显示顺序 */
    private Integer sortNum;

    /** 上传者账号主键 */
    private Long uploaderUserId;

    /** 上传时间 */
    private LocalDateTime uploadTime;

    /** 创建用于数据库映射的空附件对象。 */
    public CampusAttachment() {
    }

    /** 使用完整数据库字段创建对象，保持既有调用的字段顺序。 */
    public CampusAttachment(Long attachmentId, String bizType, Long bizId, String fileName, String bucketName, String objectKey, String contentType, Long fileSize, Integer sortNum, Long uploaderUserId, LocalDateTime uploadTime) {
        this.attachmentId = attachmentId;
        this.bizType = bizType;
        this.bizId = bizId;
        this.fileName = fileName;
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.sortNum = sortNum;
        this.uploaderUserId = uploaderUserId;
        this.uploadTime = uploadTime;
    }

}
