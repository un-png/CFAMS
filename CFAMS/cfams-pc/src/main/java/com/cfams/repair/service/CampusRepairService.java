package com.cfams.repair.service;

import com.cfams.repair.domain.dto.RepairQueryDTO;
import com.cfams.repair.domain.vo.RepairRecordVO;
import com.cfams.repair.domain.vo.RepairVO;

import java.util.List;

/** 业务服务契约；由对应实现类负责权限、事务和业务处理。 */
public interface CampusRepairService {

    /**
     * 填充当前用户可访问的数据范围。
     *
     * @param query 查询条件及数据范围
     */
    void scope(RepairQueryDTO query);

    /**
     * 按查询条件读取报修单列表。
     *
     * @param query 查询条件及数据范围
     * @return 符合条件的报修单列表
     */
    List<RepairVO> list(RepairQueryDTO query);

    /**
     * 查询报修单详情并校验当前用户的数据访问权限。
     *
     * @param id 报修单编号
     * @return 已通过权限或存在性校验的报修单
     */
    RepairVO detail(Long id);

    /**
     * 查询维修过程记录，读取前检查维修单访问权限。
     *
     * @param id 报修单编号
     * @return 按操作时间排序的维修过程记录列表
     */
    List<RepairRecordVO> records(Long id);

    /**
     * 校验权限、任务状态和维修执行资格后受理、驳回或改派，联动资产及通知。
     *
     * @param id 报修单编号
     * @param accept 是否受理
     * @param repairer 维修人员
     * @param opinion 处理意见
     * @param reassign 是否为改派操作
     */
    void assign(Long id, boolean accept, Long repairer, String opinion, boolean reassign);

    /**
     * 核验维修反馈并联动资产状态、过程记录及相关人员通知。
     *
     * @param id 报修单编号
     * @param approve 是否通过
     * @param opinion 处理意见
     */
    void verify(Long id, boolean approve, String opinion);
}
