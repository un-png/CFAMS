package com.cfams.campus.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.stereotype.Service;
import com.cfams.campus.service.CampusAccessService;
import com.cfams.campus.domain.dto.CampusQueryDTO;
import com.cfams.common.core.domain.entity.SysRole;
import com.cfams.common.exception.ServiceException;
import com.cfams.common.utils.SecurityUtils;
import com.cfams.campus.mapper.CampusAccessMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import java.util.regex.Pattern;

/** 使用现有角色数据范围；内部授权查询不得消费业务列表分页。 */
@Service
public class CampusAccessServiceImpl implements CampusAccessService {
    @Autowired
    private CampusAccessMapper campusAccessMapper;

    /** 根据当前账号的有效角色重建查询范围，并保留调用前的分页上下文。 */
    @Override
    public void scope(CampusQueryDTO query) {
        query.normalizeKeyword();
        Page<?> page = PageHelper.getLocalPage();
        PageHelper.clearPage();
        try {
            query.setUnrestricted(false);
            query.setAllowedDeptIds(Collections.emptyList());
            query.setSelfUserId(null);
            requireUser(SecurityUtils.getUserId());
            if (query.getBeginTime() != null && query.getEndTime() != null
                    && !query.getBeginTime().before(query.getEndTime())) {
                throw new ServiceException("结束时间必须晚于开始时间");
            }
            if (SecurityUtils.isAdmin()) {
                query.setUnrestricted(true);
                return;
            }
            LinkedHashSet<Long> departments = new LinkedHashSet<>();
            Long currentDepartment = campusAccessMapper.userDepartment(SecurityUtils.getUserId());
            for (SysRole role : campusAccessMapper.activeRoles(SecurityUtils.getUserId(), requestPermissions())) {
                switch (role.getDataScope()) {
                    case "1":
                        query.setUnrestricted(true);
                        break;
                    case "2":
                        departments.addAll(campusAccessMapper.roleDepartments(role.getRoleId()));
                        break;
                    case "3":
                        if (currentDepartment != null) {
                            departments.add(currentDepartment);
                        }
                        break;
                    case "4":
                        if (currentDepartment != null) {
                            departments.addAll(campusAccessMapper.descendants(currentDepartment));
                        }
                        break;
                    case "5":
                        query.setSelfUserId(SecurityUtils.getUserId());
                        break;
                    default:
                        break;
                }
            }
            query.setAllowedDeptIds(new ArrayList<>(departments));
        } finally {
            if (page != null) PageHelper.setLocalPage(page);
        }
    }

    /** 校验记录所属部门或业务所属人是否位于当前账号的数据范围内。 */
    @Override
    public void requireData(Long deptId, Long ownerId) {
        CampusQueryDTO query = new CampusQueryDTO();
        scope(query);
        if (!query.isUnrestricted() && !(deptId != null && query.getAllowedDeptIds().contains(deptId))
                && !(query.getSelfUserId() != null && Objects.equals(query.getSelfUserId(), ownerId))) {
            throw new ServiceException("无权访问该部门或业务记录", 403);
        }
    }

    /** 校验部门有效且当前账号有权在该部门办理业务。 */
    @Override
    public void requireDepartment(Long id) {
        if (id == null || campusAccessMapper.activeDepartment(id) == 0) throw new ServiceException("部门不存在或已停用");
        requireData(id, null);
    }

    /** 拒绝不存在、已删除或停用的系统账号。 */
    @Override
    public void requireUser(Long id) {
        if (id == null || campusAccessMapper.activeUser(id) == 0) throw new ServiceException("用户不存在或已停用");
    }

    /** 实时校验单项操作权限，并记录本次操作以计算角色数据范围。 */
    @Override
    public void requirePermission(String permission) {
        requireUser(SecurityUtils.getUserId());
        if (!SecurityUtils.isAdmin() && !campusAccessMapper.activePermissions(SecurityUtils.getUserId()).contains(permission)) {
            throw new ServiceException("没有此操作权限", 403);
        }
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            ServletRequestAttributes servletAttributes = (ServletRequestAttributes) attributes;
            servletAttributes.getRequest().setAttribute("campus.checkedPermission", permission);
        }
    }

    /** 校验至少一项操作权限，并仅记录实际授予的权限用于范围计算。 */
    @Override
    public void requireAnyPermission(String... permissions) {
        requireUser(SecurityUtils.getUserId());
        List<String> active = SecurityUtils.isAdmin() ? Arrays.asList(permissions) : campusAccessMapper.activePermissions(SecurityUtils.getUserId());
        List<String> granted = new ArrayList<>();
        for (String permission : permissions) {
            if (active.contains(permission)) {
                granted.add(permission);
            }
        }
        if (granted.isEmpty()) throw new ServiceException("没有此操作权限", 403);
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            ServletRequestAttributes servletAttributes = (ServletRequestAttributes) attributes;
            servletAttributes.getRequest().setAttribute("campus.checkedPermission", String.join(",", granted));
        }
    }

    /** PC入口实时核对账号和已启用菜单权限，不等待旧登录缓存失效。 */
    @Override
    public void checkRequestPermissions() {
        requireUser(SecurityUtils.getUserId());
        List<String> permissions = requestPermissions();
        if (!permissions.isEmpty() && !SecurityUtils.isAdmin()) {
            List<String> active = campusAccessMapper.activePermissions(SecurityUtils.getUserId());
            if (permissions.stream().noneMatch(active::contains)) throw new ServiceException("操作权限已撤销或停用", 403);
        }
    }

    /** 从已校验权限或控制器注解提取本次请求的校园权限标识。 */
    private List<String> requestPermissions() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            ServletRequestAttributes servletAttributes = (ServletRequestAttributes) attributes;
            Object explicit = servletAttributes.getRequest().getAttribute("campus.checkedPermission");
            if (explicit instanceof String) {
                return Arrays.asList(((String) explicit).split(","));
            }
            Object handler = servletAttributes.getRequest().getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingHandler");
            if (handler instanceof HandlerMethod) {
                HandlerMethod method = (HandlerMethod) handler;
                PreAuthorize authorization = method.getMethodAnnotation(PreAuthorize.class);
                if (authorization != null) {
                    Matcher matcher = Pattern.compile("campus:[A-Za-z]+:[A-Za-z]+").matcher(authorization.value());
                    LinkedHashSet<String> permissions = new LinkedHashSet<>();
                    while (matcher.find()) {
                        permissions.add(matcher.group());
                    }
                    return new ArrayList<>(permissions);
                }
            }
        }
        return Collections.emptyList();
    }
}
