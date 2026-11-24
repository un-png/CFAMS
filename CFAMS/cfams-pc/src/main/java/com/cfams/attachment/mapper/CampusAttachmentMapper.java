package com.cfams.attachment.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import com.cfams.attachment.domain.vo.AttachmentTargetVO;
import com.cfams.common.entity.CampusAttachment;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 业务数据访问接口；固定单表SQL使用注解，动态及关联SQL位于同业务XML。 */
public interface CampusAttachmentMapper {

    /**
     * 查询附件关联业务的资产、部门、归属人和状态投影。
     *
     * @param type 附件关联业务类型
     * @param id 附件关联业务单据或资产编号
     * @return 业务归属投影，查询不到时Mapper返回空
     */
    AttachmentTargetVO target(@Param("type") String type, @Param("id") Long id);

    /**
     * 按业务类型及编号查询有序附件列表。
     *
     * @param type 附件关联业务类型
     * @param id 附件关联业务单据或资产编号
     * @return 符合条件的业务附件列表
     */
    @Select(
            "SELECT " +
            "attachment_id,biz_type,biz_id,file_name,bucket_name,object_key,content_type,file_size,sort_num,uploader_user_id,upload_time " +
            "FROM ca_attachment WHERE biz_type=#{type} AND biz_id=#{id} ORDER BY sort_num,attachment_id")
    @ResultMap("attachmentResult")
    List<CampusAttachment> list(@Param("type") String type, @Param("id") Long id);

    /**
     * 按附件编号查询元数据。
     *
     * @param id 业务附件编号
     * @return 业务附件记录，未查询到时返回空
     */
    @Select(
            "SELECT " +
            "attachment_id,biz_type,biz_id,file_name,bucket_name,object_key,content_type,file_size,sort_num,uploader_user_id,upload_time " +
            "FROM ca_attachment WHERE attachment_id=#{id}")
    @ResultMap("attachmentResult")
    CampusAttachment detail(@Param("id") Long id);

    /**
     * 统计指定业务当前附件数量。
     *
     * @param type 附件关联业务类型
     * @param id 附件关联业务单据或资产编号
     * @return 符合条件的记录数
     */
    @Select(
            "SELECT COUNT(*) FROM ca_attachment WHERE biz_type=#{type} AND biz_id=#{id}")
    int count(@Param("type") String type, @Param("id") Long id);

    /**
     * 插入附件元数据并记录上传人及上传时间。
     *
     * @param type 附件关联业务类型
     * @param id 附件关联业务单据或资产编号
     * @param name 原始文件名
     * @param bucket OSS存储桶
     * @param key OSS对象键
     * @param contentType 文件媒体类型
     * @param size 文件字节数
     * @param userId 当前操作人用户编号
     * @return 影响行数
     */
    @Insert(
            "INSERT INTO " +
            "ca_attachment(biz_type,biz_id,file_name,bucket_name,object_key,content_type,file_size,sort_num,uploader_user_id,upload_time) " +
            "VALUES(#{type},#{id},#{name},#{bucket},#{key},#{contentType},#{size},0,#{userId},NOW(3))")
    int insert(
            @Param("type") String type,
            @Param("id") Long id,
            @Param("name") String name,
            @Param("bucket") String bucket,
            @Param("key") String key,
            @Param("contentType") String contentType,
            @Param("size") long size,
            @Param("userId") Long userId);

    /**
     * 按存储桶与对象键查询附件元数据。
     *
     * @param bucket OSS存储桶
     * @param key OSS对象键
     * @return 附件元数据
     */
    @Select(
            "SELECT " +
            "attachment_id,biz_type,biz_id,file_name,bucket_name,object_key,content_type,file_size,sort_num,uploader_user_id,upload_time " +
            "FROM ca_attachment WHERE bucket_name=#{bucket} AND object_key=#{key}")
    @ResultMap("attachmentResult")
    CampusAttachment byKey(@Param("bucket") String bucket, @Param("key") String key);
}
