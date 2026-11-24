package com.cfams.identity.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.cfams.identity.domain.dto.*;
import com.cfams.identity.domain.vo.*;
import com.cfams.common.core.domain.entity.SysUser;
import com.cfams.common.entity.CaWxBinding;

/** 人员与绑定持久化。 */
public interface CampusIdentityMapper {
    /** 按人员筛选条件和数据范围查询名册列表。 */
    List<PersonVO> people(PersonQueryDTO query);
    /** 按筛选条件和数据范围关联查询人员绑定列表。 */
    List<BindingVO> bindings(BindingQueryDTO query);
    /** 按人员ID读取名册信息。 */
    PersonVO person(@Param("id") Long id);
    /** 读取未删除且启用的关联账号并读取其部门。 */
    @Select(
            "SELECT user_id userId,dept_id deptId FROM sys_user WHERE user_id=#{id} AND status='0' AND del_flag='0'")
    SysUser selectActiveUser(@Param("id") Long id);
    /** 统计指定部门是否存在且有效。 */
    @Select(
            "SELECT COUNT(*) FROM sys_dept WHERE dept_id=#{id} AND status='0' AND del_flag='0'")
    int activeDepartment(@Param("id") Long id);
    /** 检查学工号或关联账号是否已被其他人员占用。 */
    int duplicate(@Param("id") Long id, @Param("p") PersonSaveDTO p);
    /** 插入人员名册与创建审计字段。 */
    @Insert(
            "INSERT INTO ca_person(person_no,person_type,real_name,dept_id,user_id,status,remark,create_by,create_time) " +
            "VALUES(#{p.personNo},#{p.personType},#{p.realName},#{p.deptId},#{p.userId},#{p.status},#{p.remark},#{actor},NOW(3))")
    int insert(@Param("p") PersonSaveDTO p, @Param("actor") String actor);
    /** 更新人员名册字段及修改审计信息。 */
    @Update(
            "UPDATE ca_person SET person_no=#{p.personNo},person_type=#{p.personType},real_name=#{p.realName}, " +
            "dept_id=#{p.deptId},user_id=#{p.userId},status=#{p.status},remark=#{p.remark}, " +
            "update_by=#{actor},update_time=NOW(3) WHERE person_id=#{id}")
    int update(@Param("id") Long id, @Param("p") PersonSaveDTO p, @Param("actor") String actor);
    /** 同步有效关联账号的所属部门并更新审计信息。 */
    @Update(
            "UPDATE sys_user SET dept_id=#{deptId},update_by=#{actor},update_time=NOW() WHERE user_id=#{userId} AND " +
            "status='0' AND del_flag='0'")
    int syncDepartment(@Param("userId") Long userId, @Param("deptId") Long deptId, @Param("actor") String actor);
    /** 停用人员并清除尚未使用的绑定凭据。 */
    @Update(
            "UPDATE ca_person SET status='1',bind_token_hash=NULL,token_expire_time=NULL,token_used_time=NULL, " +
            "update_by=#{actor},update_time=NOW(3) WHERE person_id=#{id}")
    int disable(@Param("id") Long id, @Param("actor") String actor);
    /** 保存绑定凭据摘要及过期时间并重置使用时间。 */
    @Update(
            "UPDATE ca_person SET bind_token_hash=#{hash},token_expire_time=#{expires},token_used_time=NULL, " +
            "update_by=#{actor},update_time=NOW(3) WHERE person_id=#{id} AND status='0'")
    int issue(@Param("id") Long id, @Param("hash") String hash, @Param("expires") Date expires, @Param("actor") String actor);
    /** 清除人员绑定凭据摘要、过期时间和使用时间。 */
    @Update(
            "UPDATE ca_person SET bind_token_hash=NULL,token_expire_time=NULL,token_used_time=NULL, " +
            "update_by=#{actor},update_time=NOW(3) WHERE person_id=#{id}")
    int invalidateToken(@Param("id") Long id, @Param("actor") String actor);
    /** 按绑定ID和所属用户删除已核验的微信绑定。 */
    @Delete(
            "DELETE FROM ca_wx_binding WHERE binding_id=#{bindingId} AND user_id=#{userId}")
    int removeBinding(CaWxBinding binding);
}
