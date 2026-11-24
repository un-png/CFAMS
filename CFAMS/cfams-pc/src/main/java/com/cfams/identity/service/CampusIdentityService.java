package com.cfams.identity.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import com.cfams.identity.domain.dto.*;
import com.cfams.identity.domain.vo.*;

/** 校园人员名册与管理员绑定核验业务契约。 */
public interface CampusIdentityService {
    /** 计算数据权限后查询人员名册。 */
    List<PersonVO> people(PersonQueryDTO query);
    /** 计算数据权限后查询人员与微信绑定管理列表。 */
    List<BindingVO> bindings(BindingQueryDTO query);
    /** 读取人员详情并验证当前用户的数据访问范围。 */
    PersonVO detail(Long id);
    /** 在事务中保存人员名册及关联账号部门。 */
    void save(Long id, PersonSaveDTO dto);
    /** 读取人员记录后停用名册并使绑定凭据失效。 */
    void disable(Long id);
    /** 解析人员Excel并在同一事务中整批校验和保存，失败时保留实际行号。 */
    PersonImportResultVO importFile(InputStream input) throws IOException;
    /** 核验人员及账号状态，生成一次性明文凭据并仅保存摘要和过期时间。 */
    BindingCredentialVO issue(Long id);
    /** 核验学工号及绑定归属后解除指定旧绑定并撤销旧凭据。 */
    void rebind(Long id, BindingRebindDTO dto);
}
