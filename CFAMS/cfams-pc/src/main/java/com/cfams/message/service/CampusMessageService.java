package com.cfams.message.service;

import com.cfams.common.entity.CampusMessage;
import com.cfams.message.domain.dto.MessageQueryDTO;

import java.util.List;

/** 业务服务契约；由对应实现类负责权限、事务和业务处理。 */
public interface CampusMessageService {

    /**
     * 按幂等事件键写入业务通知，随调用方业务事务一同提交。
     *
     * @param receiver 消息接收人编号
     * @param eventKey 消息幂等事件键
     * @param type 消息类型
     * @param bizType 关联业务类型
     * @param bizId 关联业务编号
     * @param title 通知标题
     * @param content 业务说明或消息正文
     */
    void notify(
            Long receiver,
            String eventKey,
            String type,
            String bizType,
            Long bizId,
            String title,
            String content);

    /**
     * 按当前登录用户及查询条件读取消息列表。
     *
     * @param query 查询条件及数据范围
     * @return 符合条件的当前用户消息列表
     */
    List<CampusMessage> list(MessageQueryDTO query);

    /**
     * 查询当前用户消息详情并校验当前用户的数据访问权限。
     *
     * @param id 当前用户消息编号
     * @return 已通过权限或存在性校验的当前用户消息
     */
    CampusMessage detail(Long id);

    /**
     * 校验并去重1至100条消息编号，仅将全部属于当前用户的消息标记为已读。
     *
     * @param ids 待处理消息编号集合
     */
    void read(List<Long> ids);
}
