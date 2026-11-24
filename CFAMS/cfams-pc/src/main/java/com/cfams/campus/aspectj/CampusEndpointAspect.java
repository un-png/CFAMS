package com.cfams.campus.aspectj;

import org.springframework.beans.factory.annotation.Autowired;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.RequestAttributes;
import com.cfams.campus.service.CampusAccessService;

/** 校园控制器入口实时检查权限，防止停用账号沿用旧登录凭据继续办理业务。 */
@Aspect
@Component
public class CampusEndpointAspect {
    @Autowired
    private CampusAccessService campusAccessService;

    /** 在校园控制器调用前实时校验账号和权限，防止旧令牌绕过停用。 */
    @Before("execution(public * com.cfams..controller..*(..))")
    public void check() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            ServletRequestAttributes servletAttributes = (ServletRequestAttributes) attributes;
            if (servletAttributes.getRequest().getServletPath().startsWith("/campus/")) {
                campusAccessService.checkRequestPermissions();
            }
        }
    }
}
