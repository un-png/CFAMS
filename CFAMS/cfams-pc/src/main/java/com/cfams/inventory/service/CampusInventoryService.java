package com.cfams.inventory.service;

import com.cfams.inventory.domain.dto.*;
import com.cfams.inventory.domain.vo.*;

import java.util.List;

/**
 * PC盘点管理，不包含现场扫描接口。
 */
public interface CampusInventoryService {

    /**
     * 按查询条件和数据权限查询盘点任务列表。
     */
    List<InventoryTaskVO> list(InventoryTaskQueryDTO query);

    /**
     * 查询盘点任务详情，并校验当前用户的数据访问范围。
     */
    InventoryTaskVO detail(Long id);

    /**
     * 校验草稿范围与执行人后创建盘点任务。
     */
    Long create(InventoryTaskSaveDTO request);

    /**
     * 修改尚未发布的任务草稿及执行人分配。
     */
    void edit(Long id, InventoryTaskSaveDTO request);

    /**
     * 查询草稿范围内可分配执行人的资产清单。
     */
    List<InventoryAssetVO> candidates(Long id);

    /**
     * 校验完整分配清单并按资产顺序查询，发布任务并冻结盘点快照。
     */
    void publish(Long id, InventoryPublishDTO request);

    /**
     * 校验计划开始时间后将已发布任务转入执行中。
     */
    void start(Long id);

    /**
     * 将执行中的盘点任务提交复核。
     */
    void submit(Long id);

    /**
     * 确认全部明细有复核结论后结项并通知发布人。
     */
    void complete(Long id);

    /**
     * 按查询条件和数据范围查询盘点明细。
     */
    List<InventoryItemVO> items(InventoryItemQueryDTO query);

    /**
     * 查询单条盘点明细并校验所属任务的访问权限。
     */
    InventoryItemVO item(Long id);

    /**
     * 复核盘点差异，按结论退回重盘或更新台账及维修关联。
     */
    void review(Long id, InventoryReviewDTO request);
}
