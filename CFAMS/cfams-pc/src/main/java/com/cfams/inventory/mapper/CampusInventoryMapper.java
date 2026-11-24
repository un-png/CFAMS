package com.cfams.inventory.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.cfams.campus.domain.dto.CampusQueryDTO;
import com.cfams.common.entity.CaInventoryUser;
import com.cfams.inventory.domain.InventoryRepair;
import com.cfams.inventory.domain.dto.*;
import com.cfams.inventory.domain.vo.*;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 盘点任务、固定清单与复核记录。
 */
public interface CampusInventoryMapper {

    /**
     * 按查询条件和数据权限查询盘点任务列表。
     */
    List<InventoryTaskVO> list(InventoryTaskQueryDTO query);

    /**
     * 按主键查询盘点任务详情；访问权限由服务层校验。
     */
    InventoryTaskVO detail(Long id);

    /**
     * 读取指定盘点任务，供事务内校验及更新使用。
     */
    @Select(
            "SELECT t.task_id AS taskId,t.task_no AS taskNo,t.task_name AS taskName,t.dept_id AS deptId, t.scope_dept_id " +
            "AS scopeDeptId,t.scope_location_id AS scopeLocationId,t.scope_category_id AS scopeCategoryId, t.start_time " +
            "AS startTime,t.end_time AS endTime,t.status,t.publisher_user_id AS publisherUserId, t.publish_time AS " +
            "publishTime,t.submit_time AS submitTime,t.complete_user_id AS completeUserId, t.complete_time AS " +
            "completeTime,t.create_by AS createBy,t.create_time AS createTime, t.update_time AS updateTime,t.remark FROM " +
            "ca_inventory_task t WHERE t.task_id=#{id} ")
    InventoryTaskVO selectTaskForChange(Long id);

    /**
     * 插入盘点草稿并回填任务主键。
     */
    @Insert(
            "INSERT INTO ca_inventory_task(task_no,task_name,dept_id,scope_dept_id,scope_location_id,scope_category_id, " +
            "start_time,end_time,status,create_by,create_time,remark) " +
            "VALUES(#{taskNo},#{taskName},#{deptId},#{scopeDeptId},#{scopeLocationId},#{scopeCategoryId}, " +
            "#{startTime},#{endTime},'0',#{createBy},NOW(3),#{remark})")
    @Options(useGeneratedKeys = true, useCache = false, keyProperty = "taskId")
    int insertTask(InventoryTaskVO task);

    /**
     * 仅更新尚未发布的盘点草稿。
     */
    @Update(
            "UPDATE ca_inventory_task SET task_name=#{taskName},dept_id=#{deptId},scope_dept_id=#{scopeDeptId}, " +
            "scope_location_id=#{scopeLocationId},scope_category_id=#{scopeCategoryId},start_time=#{startTime}, " +
            "end_time=#{endTime},remark=#{remark},update_by=#{createBy},update_time=NOW(3) WHERE task_id=#{taskId} AND " +
            "status='0' AND publish_time IS NULL")
    int editTask(InventoryTaskVO task);

    /**
     * 查询盘点任务执行人主键并按主键排序。
     */
    @Select(
            "SELECT user_id FROM ca_inventory_user WHERE task_id=#{taskId} ORDER BY user_id")
    List<Long> users(Long taskId);

    /**
     * 删除指定任务的执行人分配关系。
     */
    @Delete(
            "DELETE FROM ca_inventory_user WHERE task_id=#{taskId}")
    int deleteUsers(Long taskId);

    /**
     * 保存单条盘点执行人分配关系。
     */
    @Insert(
            "INSERT INTO ca_inventory_user(task_id,user_id,assigned_by,assigned_time) " +
            "VALUES(#{taskId},#{userId},#{assignedBy},NOW(3))")
    int insertUser(CaInventoryUser member);

    /**
     * 统计具有盘点执行权限的有效用户记录。
     */
    int qualifiedUser(Long userId);

    /**
     * 校验指定部门、场所及类别是否存在且启用。
     */
    int validScope(
            @Param("deptId") Long deptId,
            @Param("locationId") Long locationId,
            @Param("categoryId") Long categoryId);

    /**
     * 查询草稿范围内可分配执行人的资产清单。
     */
    List<InventoryAssetVO> candidates(
            @Param("task") InventoryTaskVO task, @Param("q") CampusQueryDTO query);

    /**
     * 为未发布草稿写入发布人和发布时间，返回受影响行数。
     */
    @Update(
            "UPDATE ca_inventory_task SET publisher_user_id=#{userId},publish_time=NOW(3), " +
            "update_by=#{operator},update_time=NOW(3) WHERE task_id=#{id} AND status='0' AND publish_time IS NULL")
    int publish(
            @Param("id") Long id, @Param("userId") Long userId, @Param("operator") String operator);

    /**
     * 插入发布时冻结的盘点明细并回填主键。
     */
    @Insert(
            "INSERT INTO " +
            "ca_inventory_item(task_id,asset_id,assigned_user_id,base_snapshot,missing_flag,review_status,create_by,create_time) " +
            "VALUES(#{taskId},#{assetId},#{assignedUserId},#{baseSnapshot},'0','0',#{createBy},NOW(3))")
    @Options(useGeneratedKeys = true, useCache = false, keyProperty = "itemId")
    int insertItem(InventoryItemVO item);

    /**
     * 按预期状态更新任务状态及提交、结项信息，返回受影响行数。
     */
    int transition(
            @Param("id") Long id,
            @Param("expected") String expected,
            @Param("status") String status,
            @Param("userId") Long userId,
            @Param("operator") String operator);

    /**
     * 按查询条件和数据范围查询盘点明细。
     */
    List<InventoryItemVO> items(InventoryItemQueryDTO query);

    /**
     * 按主键查询单条盘点明细；所属任务权限由服务层校验。
     */
    @Select(
            "SELECT i.item_id AS itemId,i.task_id AS taskId,i.asset_id AS assetId,i.assigned_user_id AS assignedUserId, " +
            "i.base_snapshot AS baseSnapshot,i.scan_time AS scanTime,i.checked_user_id AS checkedUserId, i.checked_time " +
            "AS checkedTime,i.observed_location_id AS observedLocationId, i.observed_condition AS " +
            "observedCondition,i.missing_flag AS missingFlag,i.check_note AS checkNote, i.review_status AS " +
            "reviewStatus,i.review_user_id AS reviewUserId,i.review_time AS reviewTime, i.review_note AS " +
            "reviewNote,i.review_snapshot AS reviewSnapshot,i.resolved_location_id AS resolvedLocationId, " +
            "i.resolved_condition AS resolvedCondition,i.repair_id AS repairId,i.scrap_id AS scrapId FROM " +
            "ca_inventory_item i WHERE i.item_id=#{id}")
    InventoryItemVO item(Long id);

    /**
     * 读取指定盘点明细，供事务内复核使用。
     */
    @Select(
            "SELECT i.item_id AS itemId,i.task_id AS taskId,i.asset_id AS assetId,i.assigned_user_id AS assignedUserId, " +
            "i.base_snapshot AS baseSnapshot,i.scan_time AS scanTime,i.checked_user_id AS checkedUserId, i.checked_time " +
            "AS checkedTime,i.observed_location_id AS observedLocationId, i.observed_condition AS " +
            "observedCondition,i.missing_flag AS missingFlag,i.check_note AS checkNote, i.review_status AS " +
            "reviewStatus,i.review_user_id AS reviewUserId,i.review_time AS reviewTime, i.review_note AS " +
            "reviewNote,i.review_snapshot AS reviewSnapshot,i.resolved_location_id AS resolvedLocationId, " +
            "i.resolved_condition AS resolvedCondition,i.repair_id AS repairId,i.scrap_id AS scrapId FROM " +
            "ca_inventory_item i WHERE i.item_id=#{id} ")
    InventoryItemVO selectItemForChange(Long id);

    /**
     * 更新尚未通过复核的明细结论、快照及维修关联，返回受影响行数。
     */
    @Update(
            "UPDATE ca_inventory_item SET " +
            "review_status=#{reviewStatus},review_user_id=#{reviewUserId},review_time=NOW(3), " +
            "review_note=#{reviewNote},review_snapshot=#{reviewSnapshot},resolved_location_id=#{resolvedLocationId}, " +
            "resolved_condition=#{resolvedCondition},repair_id=#{repairId},update_by=#{createBy},update_time=NOW(3) WHERE " +
            "item_id=#{itemId} AND review_status IN ('0','2')")
    int review(InventoryItemVO item);

    /**
     * 统计并读取任务下尚未通过复核的明细记录。
     */
    @Select(
            "SELECT COUNT(*) FROM ca_inventory_item WHERE task_id=#{taskId} AND review_status != '1' ")
    int unreviewed(Long taskId);

    /**
     * 统计发布后新增的资产履历，检查台账是否发生变化。
     */
    int changedSincePublish(@Param("assetId") Long assetId, @Param("taskId") Long taskId);

    /**
     * 更新未注销资产的位置及完好状态，异常时关闭借用开关。
     */
    int updateAsset(
            @Param("assetId") Long assetId,
            @Param("locationId") Long locationId,
            @Param("condition") String condition,
            @Param("operator") String operator);

    /**
     * 查询并读取资产尚未完成的报修单主键。
     */
    @Select(
            "SELECT repair_id FROM ca_repair WHERE asset_id=#{assetId} AND status IN ('0','1','2','3') ORDER BY repair_id " +
            "")
    List<Long> activeRepairs(Long assetId);

    /**
     * 统计并读取资产待审核的处置单记录。
     */
    @Select(
            "SELECT COUNT(*) FROM ca_scrap WHERE asset_id=#{assetId} AND status IN ('0','1') ")
    int pendingScrap(Long assetId);

    /**
     * 统计有效故障类型字典项。
     */
    @Select(
            "SELECT COUNT(*) FROM sys_dict_data WHERE dict_type='ca_fault_type' AND dict_value=#{faultType} AND " +
            "status='0'")
    int validFault(String faultType);

    /**
     * 插入盘点差异生成的报修单并回填维修单主键。
     */
    @Insert(
            "INSERT INTO ca_repair(repair_no,asset_id,borrow_id,reporter_user_id,asset_dept_id,fault_type, " +
            "fault_description,fault_time,fault_location,report_time,status,create_by,create_time) " +
            "VALUES(#{repairNo},#{assetId}, (SELECT b.borrow_id FROM ca_borrow b WHERE b.asset_id=#{assetId} AND b.status " +
            "IN ('3','4') ORDER BY b.borrow_id DESC LIMIT 1), " +
            "#{reporterUserId},#{assetDeptId},#{faultType},#{faultDescription},#{faultTime},#{faultLocation},NOW(3),'0',#{createBy},NOW(3))")
    @Options(useGeneratedKeys = true, useCache = false, keyProperty = "repairId")
    int insertRepair(InventoryRepair repair);
}
