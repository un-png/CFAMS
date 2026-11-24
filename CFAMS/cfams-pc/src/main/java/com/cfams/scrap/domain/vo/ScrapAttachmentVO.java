package com.cfams.scrap.domain.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 真实证明附件元数据；下载需走公共附件授权接口。
 */
@Getter
@Setter
public class ScrapAttachmentVO {

    private Long attachmentId;

    private String fileName;

    private String contentType;

    private Long fileSize;

    private Long uploaderUserId;

    private LocalDateTime uploadTime;

}
