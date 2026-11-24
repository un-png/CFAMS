package com.cfams.message.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.cfams.common.entity.CampusMessage;
import com.cfams.common.exception.ServiceException;
import com.cfams.common.utils.SecurityUtils;
import com.cfams.message.domain.dto.MessageQueryDTO;
import com.cfams.message.mapper.CampusMessageMapper;
import com.cfams.message.service.CampusMessageService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 业务服务实现，保留原有业务校验及状态处理规则。 */
@Service
public class CampusMessageServiceImpl implements CampusMessageService {

    @Autowired
    private CampusMessageMapper campusMessageMapper;

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
    @Override
    public void notify(
            Long receiver,
            String eventKey,
            String type,
            String bizType,
            Long bizId,
            String title,
            String content) {
        if (receiver == null) {
            throw new ServiceException("消息接收人不能为空");
        }
        campusMessageMapper.insert(receiver, eventKey, type, bizType, bizId, title, content);
    }

    /**
     * 按当前登录用户及查询条件读取消息列表。
     *
     * @param query 查询条件及数据范围
     * @return 符合条件的当前用户消息列表
     */
    @Override
    public List<CampusMessage> list(MessageQueryDTO query) {
        query.normalizeKeyword();
        return campusMessageMapper.list(SecurityUtils.getUserId(), query);
    }

    /**
     * 查询当前用户消息详情并校验当前用户的数据访问权限。
     *
     * @param id 当前用户消息编号
     * @return 已通过权限或存在性校验的当前用户消息
     */
    @Override
    public CampusMessage detail(Long id) {
        CampusMessage message = campusMessageMapper.detail(id, SecurityUtils.getUserId());
        if (message == null) {
            throw new ServiceException("消息不存在或无权访问", 403);
        }
        return message;
    }

    /**
     * 校验并去重1至100条消息编号，仅将全部属于当前用户的消息标记为已读。
     *
     * @param ids 待处理消息编号集合
     */
    @Transactional
    @Override
    public void read(List<Long> ids) {
        if (ids == null || ids.isEmpty() || ids.size() > 100) {
            throw new ServiceException("请选择1至100条有效消息");
        }
        for (Long id : ids) {
            if (id == null || id < 1) {
                throw new ServiceException("请选择1至100条有效消息");
            }
        }
        List<Long> unique = new java.util.ArrayList<Long>(new java.util.LinkedHashSet<Long>(ids));
        if (campusMessageMapper.ownedCount(unique, SecurityUtils.getUserId()) != unique.size()) {
            throw new ServiceException("消息不存在或不属于当前用户", 403);
        }
        campusMessageMapper.markRead(unique, SecurityUtils.getUserId());
    }
}
