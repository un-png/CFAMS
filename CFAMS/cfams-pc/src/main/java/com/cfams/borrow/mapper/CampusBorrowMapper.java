package com.cfams.borrow.mapper;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.cfams.borrow.domain.dto.BorrowQueryDTO;
import com.cfams.borrow.domain.vo.BorrowVO;
import com.cfams.borrow.domain.vo.HandoverVO;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 业务数据访问接口；固定单表SQL使用注解，动态及关联SQL位于同业务XML。 */
public interface CampusBorrowMapper {

    /**
     * 按筛选条件及数据范围查询借用单列表。
     *
     * @param query 查询条件及数据范围
     * @return 符合条件的借用单列表
     */
    List<BorrowVO> list(BorrowQueryDTO query);

    /**
     * 按数据范围查询最多100条待到期或逾期提醒的候选借用单。
     *
     * @param query 查询条件及数据范围
     * @return 符合条件的借用单列表
     */
    List<BorrowVO> reminderCandidates(BorrowQueryDTO query);

    /**
     * 按编号查询借用单及关联展示信息。
     *
     * @param id 借用单编号
     * @return 借用单记录，未查询到时返回空
     */
    BorrowVO find(@Param("id") Long id);

    /**
     * 按编号读取借用单，供事务内复核状态。
     *
     * @param id 借用单编号
     * @return 借用单记录，未查询到时返回空
     */
    @Select(
            "SELECT borrow_id,borrow_no,asset_id,asset_dept_id,applicant_user_id,purpose, " +
            "planned_start_time,planned_end_time,status,apply_time,audit_user_id,audit_time,audit_opinion, " +
            "voucher_token,actual_pickup_time,actual_return_time,cancel_user_id,cancel_time,cancel_reason FROM ca_borrow " +
            "WHERE borrow_id=#{id} ")
    BorrowVO selectForChange(@Param("id") Long id);

    /**
     * 查询借还交接列表。
     *
     * @param query 查询条件及数据范围
     * @return 符合条件的交接单列表
     */
    List<HandoverVO> handovers(BorrowQueryDTO query);

    /**
     * 按交接编号查询关联资产和申请人信息。
     *
     * @param id 交接单编号
     * @return 借用单记录，未查询到时返回空
     */
    HandoverVO handover(@Param("id") Long id);

    /**
     * 读取交接记录，供事务内复核交接状态。
     *
     * @param id 交接单编号
     * @return 借用单记录，未查询到时返回空
     */
    @Select(
            "SELECT handover_id,borrow_id,handover_type,request_user_id,request_time,request_note, " +
            "status,confirm_user_id,confirm_time,condition_status,confirm_note FROM ca_handover WHERE handover_id=#{id} " +
            "")
    HandoverVO selectHandoverForChange(@Param("id") Long id);

    /**
     * 统计同一资产的重叠预约或逾期借用，排除当前借用单。
     *
     * @param borrow 待检查冲突的借用单
     * @return 符合条件的记录数
     */
    int conflicts(BorrowVO borrow);

    /**
     * 统计资产尚未结束的报修和维修任务。
     *
     * @param assetId 资产编号
     * @return 符合条件的记录数
     */
    @Select(
            "SELECT COUNT(*) FROM ca_repair WHERE asset_id=#{assetId} AND status IN ('0','1','2','3')")
    int activeRepairs(@Param("assetId") Long assetId);

    /**
     * 统计同一资产除当前单据外已领取但未归还的借用。
     *
     * @param assetId 资产编号
     * @param id 借用单编号
     * @return 符合条件的记录数
     */
    @Select(
            "SELECT COUNT(*) FROM ca_borrow WHERE asset_id=#{assetId} AND borrow_id != #{id} AND actual_pickup_time IS " +
            "NOT NULL AND actual_return_time IS NULL")
    int unreturned(@Param("assetId") Long assetId, @Param("id") Long id);

    /**
     * 统计资产尚未完成审核的报废申请。
     *
     * @param assetId 资产编号
     * @return 符合条件的记录数
     */
    @Select(
            "SELECT COUNT(*) FROM ca_scrap WHERE asset_id=#{assetId} AND status IN ('0','1')")
    int activeScraps(@Param("assetId") Long assetId);

    /**
     * 仅更新待审核借用单的审核结果、凭证和审核人。
     *
     * @param id 借用单编号
     * @param status 业务状态
     * @param opinion 处理意见
     * @param voucher 电子凭证
     * @param userId 当前操作人用户编号
     * @param actor 当前操作人账号
     * @return 影响行数
     */
    @Update(
            "UPDATE ca_borrow SET " +
            "status=#{status},audit_opinion=#{opinion},voucher_token=#{voucher},audit_user_id=#{userId},audit_time=NOW(3),update_by=#{actor},update_time=NOW(3) " +
            "WHERE borrow_id=#{id} AND status='0'")
    int audit(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("opinion") String opinion,
            @Param("voucher") String voucher,
            @Param("userId") Long userId,
            @Param("actor") String actor);

    /**
     * 按预期状态取消尚未领取的借用单。
     *
     * @param id 借用单编号
     * @param expected 预期的变更前状态
     * @param reason 取消原因
     * @param userId 当前操作人用户编号
     * @param actor 当前操作人账号
     * @return 影响行数
     */
    @Update(
            "UPDATE ca_borrow SET " +
            "status='6',cancel_reason=#{reason},cancel_user_id=#{userId},cancel_time=NOW(3),update_by=#{actor},update_time=NOW(3) " +
            "WHERE borrow_id=#{id} AND status=#{expected} AND actual_pickup_time IS NULL")
    int cancel(
            @Param("id") Long id,
            @Param("expected") String expected,
            @Param("reason") String reason,
            @Param("userId") Long userId,
            @Param("actor") String actor);

    /**
     * 将指定借用单下待处理交接标记为驳回并保存取消原因。
     *
     * @param id 借用单编号
     * @param reason 取消原因
     * @param userId 当前操作人用户编号
     * @param actor 当前操作人账号
     * @return 影响行数
     */
    @Update(
            "UPDATE ca_handover SET " +
            "status='2',confirm_note=#{reason},confirm_user_id=#{userId},confirm_time=NOW(3),update_by=#{actor},update_time=NOW(3) " +
            "WHERE borrow_id=#{id} AND status='0'")
    int closePendingHandovers(
            @Param("id") Long id,
            @Param("reason") String reason,
            @Param("userId") Long userId,
            @Param("actor") String actor);

    /**
     * 按预期状态变更借用状态，并在领取或归还时记录实际时间。
     *
     * @param id 借用单编号
     * @param expected 预期的变更前状态
     * @param status 业务状态
     * @param actor 当前操作人账号
     * @return 影响行数
     */
    int transition(
            @Param("id") Long id,
            @Param("expected") String expected,
            @Param("status") String status,
            @Param("actor") String actor);

    /**
     * 仅更新待确认交接的处理结果、完好情况和确认人。
     *
     * @param id 交接单编号
     * @param status 业务状态
     * @param condition 交接确认的完好状态
     * @param opinion 处理意见
     * @param userId 当前操作人用户编号
     * @param actor 当前操作人账号
     * @return 影响行数
     */
    @Update(
            "UPDATE ca_handover SET " +
            "status=#{status},condition_status=#{condition},confirm_note=#{opinion},confirm_user_id=#{userId},confirm_time=NOW(3),update_by=#{actor},update_time=NOW(3) " +
            "WHERE handover_id=#{id} AND status='0'")
    int confirm(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("condition") String condition,
            @Param("opinion") String opinion,
            @Param("userId") Long userId,
            @Param("actor") String actor);
}
