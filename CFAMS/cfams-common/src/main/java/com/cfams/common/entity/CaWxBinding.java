package com.cfams.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * ca_wx_binding 持久化实体。
 * 仅用于数据映射；外部写入必须使用独立白名单DTO。
 */
@Getter
@Setter
public class CaWxBinding {
    /** 主键 */
    public Long bindingId;
    /** 关联 sys_user.user_id */
    public Long userId;
    /** 微信小程序标识 */
    public String appid;
    /** 该小程序内的微信用户标识，使用大小写敏感比较 */
    @JsonIgnore
    public String openid;
    /** 微信返回时记录，不作为必填或唯一身份依据 */
    @JsonIgnore
    public String unionid;
    /** 绑定成功时间 */
    public Date bindTime;
    /** 最后一次成功小程序登录时间 */
    public Date lastLoginTime;
    /** 0有效；1停用 */
    public String status;
    /** 创建账号，服务端填写 */
    public String createBy;
    /** 创建时间，服务端填写 */
    public Date createTime;
    /** 最后更新账号 */
    public String updateBy;
    /** 最后更新时间；不作为乐观锁版本 */
    public Date updateTime;
    /** 补充说明，不替代结构化业务字段 */
    public String remark;

}
