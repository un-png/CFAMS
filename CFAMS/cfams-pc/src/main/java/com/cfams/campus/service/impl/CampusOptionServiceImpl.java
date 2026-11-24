package com.cfams.campus.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import org.springframework.stereotype.Service;
import com.cfams.campus.service.CampusAccessService;
import com.cfams.campus.domain.vo.CampusOptionVO;
import com.cfams.campus.domain.dto.CampusQueryDTO;
import com.cfams.campus.mapper.CampusOptionMapper;
import com.cfams.campus.service.CampusOptionService;
import com.cfams.common.exception.ServiceException;

/** 按当前账号数据范围读取页面下拉选项。 */
@Service
public class CampusOptionServiceImpl implements CampusOptionService {
    @Autowired
    private CampusAccessService campusAccessService;
    @Autowired
    private CampusOptionMapper campusOptionMapper;

    /** 先计算数据权限，再按类型查询部门、类别、场所、人员或资产。 */
    @Override
    public List<CampusOptionVO> selectOptions(String type, CampusQueryDTO query) {
        campusAccessService.scope(query);
        switch (type) {
            case "departments":
                return campusOptionMapper.departments(query);
            case "categories":
                return campusOptionMapper.categories();
            case "locations":
                return campusOptionMapper.locations(query);
            case "users":
                return campusOptionMapper.users(query, null);
            case "repairers":
                return campusOptionMapper.users(query, "campus:repair:execute");
            case "inventoryUsers":
                return campusOptionMapper.users(query, "campus:inventory:execute");
            case "assets":
                return campusOptionMapper.assets(query);
            default:
                throw new ServiceException("不支持的选项类型");
        }
    }
}
