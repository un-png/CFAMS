package com.cfams.campus.mapper;

import org.apache.ibatis.annotations.Select;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.cfams.common.core.domain.entity.SysRole;

/** 校园业务账号、角色权限及部门范围查询，不承载资产表操作。 */
public interface CampusAccessMapper {
    /** 查询用户仍有效且授予指定操作权限的角色及数据范围配置。 */
    List<SysRole> activeRoles(@Param("userId") Long userId, @Param("permissions") List<String> permissions);
    /** 查询指定角色配置的自定义授权部门。 */
    @Select(
            "SELECT dept_id FROM sys_role_dept WHERE role_id=#{roleId}")
    List<Long> roleDepartments(@Param("roleId") Long roleId);
    /** 查询部门本身及下级有效部门的主键。 */
    @Select(
            "SELECT dept_id FROM sys_dept WHERE del_flag='0' AND (dept_id=#{deptId} OR FIND_IN_SET(#{deptId},ancestors))")
    List<Long> descendants(@Param("deptId") Long deptId);
    /** 统计指定账号是否存在且未删除、未停用。 */
    @Select(
            "SELECT COUNT(*) FROM sys_user WHERE user_id=#{id} AND status='0' AND del_flag='0'")
    int activeUser(@Param("id") Long id);
    /** 查询指定系统账号的所属部门。 */
    @Select(
            "SELECT dept_id FROM sys_user WHERE user_id=#{id} AND status='0' AND del_flag='0'")
    Long userDepartment(@Param("id") Long id);
    /** 统计指定部门是否存在且未删除、未停用。 */
    @Select(
            "SELECT COUNT(*) FROM sys_dept WHERE dept_id=#{id} AND status='0' AND del_flag='0'")
    int activeDepartment(@Param("id") Long id);
    /** 查询用户由有效角色和有效菜单获得的操作权限。 */
    List<String> activePermissions(@Param("userId") Long userId);
}
