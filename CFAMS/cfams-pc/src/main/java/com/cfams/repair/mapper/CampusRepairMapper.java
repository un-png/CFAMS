package com.cfams.repair.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import com.cfams.repair.domain.dto.RepairQueryDTO;
import com.cfams.repair.domain.vo.RepairRecordVO;
import com.cfams.repair.domain.vo.RepairVO;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 业务数据访问接口；固定单表SQL使用注解，动态及关联SQL位于同业务XML。 */
public interface CampusRepairMapper {

    /**
     * 按筛选条件及数据范围查询报修单列表。
     *
     * @param query 查询条件及数据范围
     * @return 符合条件的报修单列表
     */
    List<RepairVO> list(RepairQueryDTO query);

    /**
     * 按编号查询报修单及关联展示信息。
     *
     * @param id 报修单编号
     * @return 报修单记录，未查询到时返回空
     */
    RepairVO find(@Param("id") Long id);

    /**
     * 按编号读取报修单，供事务内复核状态。
     *
     * @param id 报修单编号
     * @return 报修单记录，未查询到时返回空
     */
    @Select(
            "SELECT repair_id,repair_no,asset_id,asset_dept_id,borrow_id,reporter_user_id, " +
            "fault_type,fault_description,fault_time,fault_location,report_time,status, " +
            "accept_user_id,accept_time,accept_opinion,repairer_user_id,assign_time,start_time, " +
            "repair_plan,repair_cost,repair_result,result_description,feedback_time, " +
            "verify_user_id,verify_time,verify_opinion FROM ca_repair WHERE repair_id=#{id} ")
    RepairVO selectForChange(@Param("id") Long id);

    /**
     * 按操作时间和记录编号查询维修过程记录。
     *
     * @param id 报修单编号
     * @return 按操作时间排序的维修过程记录列表
     */
    List<RepairRecordVO> records(@Param("id") Long id);

    /**
     * 统计指定用户是否启用且拥有维修执行权限。
     *
     * @param id 报修单编号
     * @return 符合条件的记录数
     */
    int qualifiedRepairer(@Param("id") Long id);

    /**
     * 统计资产处于借用中或待归还确认状态的借用单。
     *
     * @param assetId 资产编号
     * @return 符合条件的记录数
     */
    @Select(
            "SELECT COUNT(*) FROM ca_borrow WHERE asset_id=#{assetId} AND status IN ('3','4')")
    int activeBorrow(@Param("assetId") Long assetId);

    /**
     * 统计同一资产除当前任务外尚未结束的维修任务。
     *
     * @param assetId 资产编号
     * @param id 报修单编号
     * @return 符合条件的记录数
     */
    @Select(
            "SELECT COUNT(*) FROM ca_repair WHERE asset_id=#{assetId} AND repair_id != #{id} AND status IN " +
            "('0','1','2','3')")
    int otherRepairs(@Param("assetId") Long assetId, @Param("id") Long id);

    /**
     * 按预期状态更新报修受理结果或维修人员指派信息。
     *
     * @param id 报修单编号
     * @param expected 预期的变更前状态
     * @param status 业务状态
     * @param repairer 维修人员
     * @param opinion 处理意见
     * @param userId 当前操作人用户编号
     * @param actor 当前操作人账号
     * @return 影响行数
     */
    int assign(
            @Param("id") Long id,
            @Param("expected") String expected,
            @Param("status") String status,
            @Param("repairer") Long repairer,
            @Param("opinion") String opinion,
            @Param("userId") Long userId,
            @Param("actor") String actor);

    /**
     * 仅更新待核验维修单的结果，通过时保存核验人和核验意见。
     *
     * @param id 报修单编号
     * @param status 业务状态
     * @param opinion 处理意见
     * @param userId 当前操作人用户编号
     * @param actor 当前操作人账号
     * @return 影响行数
     */
    int verify(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("opinion") String opinion,
            @Param("userId") Long userId,
            @Param("actor") String actor);

    /**
     * 插入维修过程记录并回填自增记录编号。
     *
     * @param record 维修过程记录
     * @return 影响行数
     */
    @Insert(
            "INSERT INTO " +
            "ca_repair_record(repair_id,action_type,from_status,to_status,operator_user_id,target_user_id,content,cost_snapshot,result_snapshot,action_time) " +
            "VALUES(#{repairId},#{actionType},#{fromStatus},#{toStatus},#{operatorUserId},#{targetUserId},#{content},#{costSnapshot},#{resultSnapshot},NOW(3))")
    @Options(useGeneratedKeys = true, useCache = false, keyProperty = "recordId")
    int record(RepairRecordVO record);
}
