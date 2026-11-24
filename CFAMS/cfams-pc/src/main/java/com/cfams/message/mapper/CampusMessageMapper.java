package com.cfams.message.mapper;

import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import com.cfams.common.entity.CampusMessage;
import com.cfams.message.domain.dto.MessageQueryDTO;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 业务数据访问接口；固定单表SQL使用注解，动态及关联SQL位于同业务XML。 */
public interface CampusMessageMapper {

    /**
     * 写入消息，事件键重复时保持原消息以实现幂等。
     *
     * @param receiver 消息接收人编号
     * @param eventKey 消息幂等事件键
     * @param type 消息类型
     * @param bizType 关联业务类型
     * @param bizId 关联业务编号
     * @param title 通知标题
     * @param content 业务说明或消息正文
     * @return 影响行数
     */
    int insert(
            @Param("receiver") Long receiver,
            @Param("eventKey") String eventKey,
            @Param("type") String type,
            @Param("bizType") String bizType,
            @Param("bizId") Long bizId,
            @Param("title") String title,
            @Param("content") String content);

    /**
     * 按接收人和筛选条件查询消息列表。
     *
     * @param userId 当前操作人用户编号
     * @param query 查询条件及数据范围
     * @return 符合条件的当前用户消息列表
     */
    List<CampusMessage> list(@Param("userId") Long userId, @Param("q") MessageQueryDTO query);

    /**
     * 查询指定接收人拥有的单条消息。
     *
     * @param id 当前用户消息编号
     * @param userId 当前操作人用户编号
     * @return 当前用户消息记录，未查询到时返回空
     */
    @Select(
            "SELECT " +
            "message_id,message_type,biz_type,biz_id,title,content,read_flag,read_time,create_time,receiver_user_id,event_key " +
            "FROM ca_message WHERE message_id=#{id} AND receiver_user_id=#{userId}")
    @ResultMap("messageResult")
    CampusMessage detail(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 将指定用户拥有的消息标为已读，保留首次阅读时间。
     *
     * @param ids 待处理消息编号集合
     * @param userId 当前操作人用户编号
     * @return 影响行数
     */
    int markRead(@Param("ids") List<Long> ids, @Param("userId") Long userId);

    /**
     * 统计消息编号集合中属于指定用户的记录数。
     *
     * @param ids 待处理消息编号集合
     * @param userId 当前操作人用户编号
     * @return 符合条件的记录数
     */
    int ownedCount(@Param("ids") List<Long> ids, @Param("userId") Long userId);
}
