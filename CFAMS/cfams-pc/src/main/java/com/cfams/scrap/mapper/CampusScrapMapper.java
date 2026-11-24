package com.cfams.scrap.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import com.cfams.scrap.domain.dto.*;
import com.cfams.scrap.domain.vo.*;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 处置单和审批的持久化；调用者先校验资产状态，状态变更使用条件更新。
 */
public interface CampusScrapMapper {

    /**
     * 按查询条件和数据权限查询处置单列表。
     */
    List<ScrapVO> list(ScrapQueryDTO query);

    /**
     * 按主键查询处置单及资产展示字段；访问权限由服务层校验。
     */
    ScrapVO detail(Long id);

    /**
     * 读取指定处置单，供审核事务使用。
     */
    @Select(
            "SELECT s.scrap_id AS scrapId, s.scrap_no AS scrapNo, s.asset_id AS assetId, s.asset_dept_id AS assetDeptId, " +
            "s.applicant_user_id AS applicantUserId, s.dispose_type AS disposeType, s.reason, s.apply_time AS applyTime, " +
            "s.status, s.completed_time AS completedTime, s.create_by AS createBy FROM ca_scrap s WHERE s.scrap_id = " +
            "#{id} ")
    ScrapVO selectForChange(Long id);

    /**
     * 插入处置申请并回填处置单主键。
     */
    @Insert(
            "INSERT INTO ca_scrap(scrap_no,asset_id,asset_dept_id,applicant_user_id,dispose_type,reason, " +
            "apply_time,status,create_by,create_time) " +
            "VALUES(#{scrapNo},#{assetId},#{assetDeptId},#{applicantUserId},#{disposeType},#{reason}, " +
            "NOW(3),'0',#{createBy},NOW(3))")
    @Options(useGeneratedKeys = true, useCache = false, keyProperty = "scrapId")
    int insert(ScrapVO scrap);

    /**
     * 统计并读取资产尚未结束的借还业务。
     */
    @Select(
            "SELECT COUNT(*) FROM ca_borrow WHERE asset_id=#{assetId} AND status IN ('0','2','3','4') ")
    int unfinishedBorrow(Long assetId);

    /**
     * 统计并读取资产尚未完成的维修业务。
     */
    @Select(
            "SELECT COUNT(*) FROM ca_repair WHERE asset_id=#{assetId} AND status IN ('0','1','2','3') ")
    int unfinishedRepair(Long assetId);

    /**
     * 排除指定处置单后统计并读取资产待审核的处置单记录。
     */
    int pendingScrap(@Param("assetId") Long assetId, @Param("excludeId") Long excludeId);

    /**
     * 查询处置单的审核记录及审核人姓名。
     */
    List<ScrapAuditVO> audits(Long id);

    /**
     * 查询处置单关联的真实证明附件元数据。
     */
    @Select(
            "SELECT attachment_id AS attachmentId,file_name AS fileName,content_type AS contentType, file_size AS " +
            "fileSize,uploader_user_id AS uploaderUserId,upload_time AS uploadTime FROM ca_attachment WHERE " +
            "biz_type='SCRAP' AND biz_id=#{id} ORDER BY sort_num,attachment_id")
    List<ScrapAttachmentVO> attachments(Long id);

    /**
     * 读取具有有效存储信息且非空的处置证明附件。
     */
    @Select(
            "SELECT attachment_id FROM ca_attachment WHERE biz_type='SCRAP' AND biz_id=#{id} AND file_size > 0 AND " +
            "bucket_name != '' AND object_key != '' ORDER BY attachment_id ")
    List<Long> selectProofIds(Long id);

    /**
     * 插入审核记录并回填审核记录主键。
     */
    @Insert(
            "INSERT INTO ca_scrap_audit(scrap_id,audit_level,audit_result,auditor_user_id,audit_opinion,audit_time) " +
            "VALUES(#{scrapId},#{auditLevel},#{auditResult},#{auditorUserId},#{auditOpinion},NOW(3))")
    @Options(useGeneratedKeys = true, useCache = false, keyProperty = "auditId")
    int insertAudit(ScrapAuditVO audit);

    /**
     * 按预期状态更新处置单，终审通过时写入完成时间并返回受影响行数。
     */
    int transition(
            @Param("id") Long id,
            @Param("expected") String expected,
            @Param("status") String status,
            @Param("operator") String operator);
}
