package com.cfams.statistics.domain.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 当前分类或部门分布。
 */
@Getter
@Setter
public class DistributionVO {

    private Long id;

    private String name;

    private long count;

    private BigDecimal value;

}
