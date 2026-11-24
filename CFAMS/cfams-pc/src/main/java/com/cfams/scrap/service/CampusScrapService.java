package com.cfams.scrap.service;

import com.cfams.scrap.domain.dto.*;
import com.cfams.scrap.domain.vo.*;

import java.util.List;

/**
 * PC报废固定两级审批。
 */
public interface CampusScrapService {

    /**
     * 按查询条件和数据权限查询处置单列表。
     */
    List<ScrapVO> list(ScrapQueryDTO query);

    /**
     * 查询处置详情、审核记录及证明附件并校验访问权限。
     */
    ScrapVO detail(Long id);

    /**
     * 校验资产及未完成业务后创建处置申请。
     */
    Long create(ScrapCreateDTO request);

    /**
     * 执行固定两级审核，终审通过时注销资产并记录履历和通知。
     */
    void audit(Long id, String level, ScrapAuditDTO request);
}
