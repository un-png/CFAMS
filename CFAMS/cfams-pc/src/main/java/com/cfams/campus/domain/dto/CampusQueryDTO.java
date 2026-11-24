package com.cfams.campus.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import com.cfams.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.format.annotation.DateTimeFormat;

/** 校园查询参数；数据范围由服务端重新填充。 */
@Getter
@Setter
public class CampusQueryDTO extends BaseEntity {
    private String keyword;
    private String status;
    private String conditionStatus;
    private String locationType;
    private String disposeType;
    private String messageType;
    private Boolean handled;
    private String applicant;
    private Long deptId;
    private Long categoryId;
    private Long locationId;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date beginTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    @JsonIgnore private List<Long> allowedDeptIds = java.util.Collections.emptyList();
    @JsonIgnore private Long selfUserId;
    @JsonIgnore private boolean unrestricted;

    /** 在业务查询前清理关键字首尾空白，保留原查询参数规范化行为。 */
    public void normalizeKeyword() {
        if (keyword != null) {
            keyword = keyword.trim();
        }
    }
}
