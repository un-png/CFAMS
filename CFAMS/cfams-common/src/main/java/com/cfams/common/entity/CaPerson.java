package com.cfams.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * ca_person 持久化实体。
 * 仅用于数据映射；外部写入必须使用独立白名单DTO。
 */
@Getter
@Setter
public class CaPerson {
    /** 主键 */
    public Long personId;
    /** 全校唯一学号或工号；作为字符串保留前导零 */
    public String personNo;
    /** 1学生；2教职工 */
    public String personType;
    /** 名册真实姓名，不使用昵称充当核验依据 */
    public String realName;
    /** 关联 sys_dept.dept_id；人员当前所属部门 */
    public Long deptId;
    /** 关联 sys_user.user_id；未关联账号时为空 */
    public Long userId;
    /** 0有效；1停用；不替代账号停用状态 */
    public String status;
    /** 一次性绑定凭据的安全摘要，不保存明文 */
    @JsonIgnore
    public String bindTokenHash;
    /** 绑定凭据有效截止时间 */
    @JsonIgnore
    public Date tokenExpireTime;
    /** 凭据成功使用时间；补发时清空并替换摘要 */
    @JsonIgnore
    public Date tokenUsedTime;
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
