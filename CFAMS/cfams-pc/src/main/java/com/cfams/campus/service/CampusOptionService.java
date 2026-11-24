package com.cfams.campus.service;

import java.util.List;
import com.cfams.campus.domain.vo.CampusOptionVO;
import com.cfams.campus.domain.dto.CampusQueryDTO;

/** 授权业务选项服务。 */
public interface CampusOptionService {
    /** 根据选项类型和查询条件返回当前用户可选择的业务记录。 */
    List<CampusOptionVO> selectOptions(String type, CampusQueryDTO query);
}
