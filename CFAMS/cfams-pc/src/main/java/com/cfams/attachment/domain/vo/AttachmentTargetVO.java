package com.cfams.attachment.domain.vo;

import lombok.Getter;
import lombok.Setter;

/** 附件归属的权限和状态投影。 */
@Getter
@Setter
public class AttachmentTargetVO {

    private Long assetId;

    private Long deptId;

    private Long ownerId;

    private String status;

    /** 初始化附件归属投影。 */
    public AttachmentTargetVO() {}

    /**
     * 初始化附件归属投影。
     *
     * @param assetId 资产编号
     * @param deptId 所属部门编号
     * @param ownerId 业务归属人编号
     * @param status 业务状态
     */
    public AttachmentTargetVO(Long assetId, Long deptId, Long ownerId, String status) {
        this.assetId = assetId;
        this.deptId = deptId;
        this.ownerId = ownerId;
        this.status = status;
    }

}
