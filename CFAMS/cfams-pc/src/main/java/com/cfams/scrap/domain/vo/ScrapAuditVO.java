package com.cfams.scrap.domain.vo;

import lombok.Getter;
import lombok.Setter;

import com.cfams.common.entity.CaScrapAudit;

/**
 * 查询投影：持久化字段继承实体，附加展示字段；保持原JSON字段集合。
 */
@Getter
@Setter
public class ScrapAuditVO extends CaScrapAudit {

    private String auditorName;

}
