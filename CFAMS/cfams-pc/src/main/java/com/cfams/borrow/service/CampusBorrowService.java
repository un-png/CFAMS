package com.cfams.borrow.service;

import com.cfams.borrow.domain.dto.BorrowQueryDTO;
import com.cfams.borrow.domain.vo.BorrowVO;
import com.cfams.borrow.domain.vo.HandoverVO;

import java.util.List;

/** 业务服务契约；由对应实现类负责权限、事务和业务处理。 */
public interface CampusBorrowService {

    /**
     * 填充当前用户数据范围及业务查询配置。
     *
     * @param query 查询条件及数据范围
     */
    void scope(BorrowQueryDTO query);

    /**
     * 按查询条件读取借用单列表。
     *
     * @param query 查询条件及数据范围
     * @return 符合条件的借用单列表
     */
    List<BorrowVO> list(BorrowQueryDTO query);

    /**
     * 分页开始前扫描提醒候选借用单，逐单以独立事务复核状态并幂等发送提醒。
     *
     * @param query 查询条件及数据范围
     */
    void remind(BorrowQueryDTO query);

    /**
     * 查询借还交接列表。
     *
     * @param query 查询条件及数据范围
     * @return 符合条件的交接单列表
     */
    List<HandoverVO> handovers(BorrowQueryDTO query);

    /**
     * 查询借用单详情并校验当前用户的数据访问权限。
     *
     * @param id 借用单编号
     * @return 已通过权限或存在性校验的借用单
     */
    BorrowVO detail(Long id);

    /**
     * 查询单条交接记录并校验数据权限。
     *
     * @param id 交接单编号
     * @return 已通过权限校验的交接单
     */
    HandoverVO handover(Long id);

    /**
     * 校验审核权限与预约冲突后审核借用申请，同步凭证、历史及通知。
     *
     * @param id 借用单编号
     * @param approve 是否通过
     * @param opinion 处理意见
     */
    void audit(Long id, boolean approve, String opinion);

    /**
     * 取消尚未交付的预约并关闭待处理交接，记录历史并通知申请人。
     *
     * @param id 借用单编号
     * @param reason 取消原因
     */
    void cancel(Long id, String reason);

    /**
     * 核对人员、凭证及单据状态后确认或驳回交接，同步借用和资产状态。
     *
     * @param id 交接单编号
     * @param approve 是否通过
     * @param condition 交接确认的完好状态
     * @param opinion 处理意见
     * @param voucher 电子凭证
     */
    void confirm(Long id, boolean approve, String condition, String opinion, String voucher);
}
