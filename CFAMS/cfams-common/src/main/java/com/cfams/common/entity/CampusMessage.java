package com.cfams.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
/** 业务消息实体，接收人和去重键不进入外部JSON。 */
@Getter
@Setter
public class CampusMessage {
    /** 消息主键 */
    private Long messageId;

    /** 业务消息类型 */
    private String messageType;

    /** 所属业务类型 */
    private String bizType;

    /** 关联业务主键 */
    private Long bizId;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 消息已读标记 */
    private String readFlag;

    /** 消息阅读时间 */
    private LocalDateTime readTime;

    /** 消息创建时间 */
    private LocalDateTime createTime;

    /** 消息接收人主键 */
    @JsonIgnore
    private Long receiverUserId;

    /** 业务事件去重键 */
    @JsonIgnore
    private String eventKey;

    /** 创建用于数据库映射的空消息对象。 */
    public CampusMessage() {
    }

    /** 使用完整数据库字段创建对象，保持既有调用的字段顺序。 */
    public CampusMessage(Long messageId, String messageType, String bizType, Long bizId, String title, String content, String readFlag, LocalDateTime readTime, LocalDateTime createTime, Long receiverUserId, String eventKey) {
        this.messageId = messageId;
        this.messageType = messageType;
        this.bizType = bizType;
        this.bizId = bizId;
        this.title = title;
        this.content = content;
        this.readFlag = readFlag;
        this.readTime = readTime;
        this.createTime = createTime;
        this.receiverUserId = receiverUserId;
        this.eventKey = eventKey;
    }

}
