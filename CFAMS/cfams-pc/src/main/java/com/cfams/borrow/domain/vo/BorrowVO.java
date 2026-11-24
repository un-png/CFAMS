package com.cfams.borrow.domain.vo;

import lombok.Getter;
import lombok.Setter;

import com.cfams.common.entity.CaBorrow;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** 查询投影：持久化字段继承实体，附加展示字段；保持原JSON字段集合。 */
@JsonIgnoreProperties({
    "applicantDeptId",
    "createBy",
    "createTime",
    "updateBy",
    "updateTime",
    "remark"
})
@Getter
@Setter
public class BorrowVO extends CaBorrow {

    public String assetCode;

    public String assetName;

    public String applicantName;

}
