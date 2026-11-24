package com.cfams.asset.service;

import java.util.List;
import com.cfams.asset.domain.dto.*;
import com.cfams.asset.domain.vo.*;
import com.cfams.asset.domain.dto.AssetQueryDTO;
import com.cfams.asset.domain.dto.CategoryQueryDTO;
import com.cfams.asset.domain.dto.LocationQueryDTO;
import com.cfams.campus.domain.dto.CampusQueryDTO;

/** 资产台账、类别、场所及标签查询业务契约。 */
public interface CampusAssetService {
    /** 计算当前用户的数据范围并校验查询时间区间，须在启动分页前调用。 */
    void scope(CampusQueryDTO query);
    /** 按关键字和启停状态查询资产类别列表。 */
    List<CategoryVO> categories(CategoryQueryDTO query);
    /** 按筛选条件和授权部门范围查询场所列表。 */
    List<LocationVO> locations(LocationQueryDTO query);
    /** 按筛选条件和用户数据范围查询资产台账。 */
    List<AssetVO> assets(AssetQueryDTO query);
    /** 查询指定资产按时间倒序排列的变更履历。 */
    List<AssetHistoryVO> history(Long id);
    /** 读取指定资产类别详情。 */
    CategoryVO category(Long id);
    /** 读取指定场所详情并核对部门范围。 */
    LocationVO location(Long id);
    /** 读取指定资产详情并核对部门及责任人数据权限。 */
    AssetVO asset(Long id);
    /** 校验二维码定位标识并返回用户有权查看的资产。 */
    AssetVO resolve(String token);
    /** 校验资产数量及访问权限，生成去重后的稳定标签数据。 */
    List<AssetLabelVO> labels(List<Long> ids);
    /** 在事务中校验类别编码、树结构及停用引用条件后保存类别。 */
    CategoryVO saveCategory(CategorySaveDTO value, boolean create);
    /** 在事务中校验场所树结构、部门权限及停用条件后保存场所。 */
    LocationVO saveLocation(LocationSaveDTO value, boolean create);
    /** 校验台账参数和关联数据后入册，并记录资产登记履历。 */
    AssetVO create(AssetSaveDTO value);
    /** 读取在库资产，校验未结业务和编号后更新台账及变更履历。 */
    AssetVO update(AssetSaveDTO value);
    /** 为整批导入预校验资产字段、编号唯一性和关联数据，此阶段不写入。 */
    void validateNewAsset(AssetSaveDTO value);
    /** 在调用方已经开启的整批事务内写入已校验资产并记录登记履历。 */
    void insertValidated(AssetSaveDTO value);
}
