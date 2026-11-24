package com.cfams.scrap.domain.vo;

import lombok.Getter;
import lombok.Setter;

import com.cfams.common.entity.CaScrap;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * 查询投影：持久化字段继承实体，附加展示字段；保持原JSON字段集合。
 */
@JsonIgnoreProperties({"createTime", "updateBy", "updateTime", "remark"})
@Getter
@Setter
public class ScrapVO extends CaScrap {

    private String assetCode;

    private String assetName;

    private String deptName;

    private String applicantName;

    private List<ScrapAuditVO> audits;

    private List<ScrapAttachmentVO> attachments;

}
