package com.cfams.repair.domain.dto;

import lombok.Getter;
import lombok.Setter;

import com.cfams.campus.domain.dto.CampusQueryDTO;

/** 业务查询或请求数据对象。 */
@Getter
@Setter
public class RepairQueryDTO extends CampusQueryDTO {

    private String faultType;

    private String repairer;

}
