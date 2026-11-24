package com.cfams.borrow.domain.dto;

import lombok.Getter;
import lombok.Setter;

import com.cfams.campus.domain.dto.CampusQueryDTO;

/** 业务查询或请求数据对象。 */
@Getter
@Setter
public class BorrowQueryDTO extends CampusQueryDTO {

    private String applicant;

    private String handoverType;

    private String timing;

    private int remindHours = 24;

}
