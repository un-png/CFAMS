package com.cfams.identity.domain.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/** 绑定凭据发放结果 */
@Getter
@Setter
public class BindingCredentialVO {
    private Long personId;
    private String token;
    private Date expireTime;

    /** 创建空的绑定凭据发放结果，供请求绑定或逐项赋值使用。 */
    public BindingCredentialVO() {
    }

    /** 使用完整字段创建绑定凭据发放结果。 */
    public BindingCredentialVO(Long personId, String token, Date expireTime) {
        this.personId = personId;
        this.token = token;
        this.expireTime = expireTime;
    }

}
