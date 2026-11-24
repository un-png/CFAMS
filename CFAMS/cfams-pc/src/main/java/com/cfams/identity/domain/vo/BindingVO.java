package com.cfams.identity.domain.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/** 绑定管理视图；不暴露 openid、unionid 或凭据摘要。 */
@Getter
@Setter
public class BindingVO extends PersonVO {
    private Long bindingId;
    private String appid;
    private String bindingStatus;
    private Date bindTime;
    private Date lastLoginTime;

}
