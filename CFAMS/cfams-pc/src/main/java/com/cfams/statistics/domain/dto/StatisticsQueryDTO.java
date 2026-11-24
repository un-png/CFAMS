package com.cfams.statistics.domain.dto;

import lombok.Getter;
import lombok.Setter;

import com.cfams.campus.domain.dto.CampusQueryDTO;

/**
 * 时间范围为[beginTime,endTime)，分组维度由白名单限定。
 */
@Getter
@Setter
public class StatisticsQueryDTO extends CampusQueryDTO {

    private String reportType = "ALL";

    private String dimension = "MONTH";

    private Integer minUseYears;

    private Integer maxUseYears;

    private Long pendingUserId;

    private boolean pendingAdmin;

}
