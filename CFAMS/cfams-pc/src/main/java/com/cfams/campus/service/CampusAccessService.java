package com.cfams.campus.service;

import com.cfams.campus.domain.dto.CampusQueryDTO;

/** 校园业务的账号有效性、操作权限和数据范围校验服务。 */
public interface CampusAccessService {
    /** 根据当前账号有效角色计算查询范围，并保留调用前的分页上下文。 */
    void scope(CampusQueryDTO query);

    /** 校验业务记录的部门或所属人是否在当前账号数据范围内。 */
    void requireData(Long deptId, Long ownerId);

    /** 校验部门有效且当前账号有权访问该部门。 */
    void requireDepartment(Long id);

    /** 校验账号存在且未删除、未停用。 */
    void requireUser(Long id);

    /** 实时校验指定操作权限并记录本次操作的数据范围依据。 */
    void requirePermission(String permission);

    /** 校验至少一项操作权限，并记录实际授予的权限以计算数据范围。 */
    void requireAnyPermission(String... permissions);

    /** 进入校园接口时实时校验账号状态与有效菜单权限。 */
    void checkRequestPermissions();
}
