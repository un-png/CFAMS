package com.cfams.borrow.domain.vo;

import lombok.Getter;
import lombok.Setter;

import com.cfams.common.entity.CaHandover;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** 查询投影：持久化字段继承实体，附加展示字段；保持原JSON字段集合。 */
@JsonIgnoreProperties({"requestKey", "createBy", "createTime", "updateBy", "updateTime", "remark"})
@Getter
@Setter
public class HandoverVO extends CaHandover {

    public Long assetId;

    public Long assetDeptId;

    public Long applicantUserId;

    public String borrowNo;

    public String assetName;

    public String applicantName;

}
