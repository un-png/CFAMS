package com.cfams.asset.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import com.cfams.asset.domain.vo.AssetStateVO;
import org.apache.ibatis.annotations.Param;
import com.cfams.asset.domain.dto.*;
import com.cfams.asset.domain.vo.*;
import com.cfams.asset.domain.dto.AssetQueryDTO;
import com.cfams.asset.domain.dto.CategoryQueryDTO;
import com.cfams.asset.domain.dto.LocationQueryDTO;

/** 台账及基础配置的原生 MyBatis 映射。 */
public interface CampusAssetMapper {
    /** 按关键字和启停状态查询资产类别列表。 */
    List<CategoryVO> categories(CategoryQueryDTO query);
    /** 按ID顺序读取类别树，供校验层级结构。 */
    @Select(
            "SELECT category_id AS categoryId, parent_id AS parentId, category_code AS categoryCode, category_name AS " +
            "categoryName, order_num AS orderNum, status, remark FROM ca_category ORDER BY category_id ")
    List<CategoryVO> selectCategoryTree();
    /** 读取指定资产类别详情。 */
    @Select(
            "SELECT category_id AS categoryId, parent_id AS parentId, category_code AS categoryCode, category_name AS " +
            "categoryName, order_num AS orderNum, status, remark FROM ca_category WHERE category_id = #{id}")
    CategoryVO category(Long id);
    /** 读取指定资产类别并返回当前状态。 */
    @Select(
            "SELECT category_id AS categoryId, parent_id AS parentId, category_code AS categoryCode, category_name AS " +
            "categoryName, order_num AS orderNum, status, remark FROM ca_category WHERE category_id = #{id} ")
    CategoryVO selectCategoryForChange(Long id);
    /** 统计引用指定类别的资产数量。 */
    @Select(
            "SELECT COUNT(1) FROM ca_asset WHERE category_id = #{id}")
    int categoryReferences(Long id);
    /** 插入类别及创建审计字段并回填主键。 */
    @Insert(
            "INSERT INTO ca_category(parent_id, category_code, category_name, order_num, status, remark, create_by, " +
            "create_time) VALUES(#{v.parentId}, #{v.categoryCode}, #{v.categoryName}, #{v.orderNum}, #{v.status}, " +
            "#{v.remark}, #{actor}, NOW(3))")
    @Options(useGeneratedKeys = true, useCache = false, keyProperty = "v.categoryId")
    int insertCategory(@Param("v") CategorySaveDTO value, @Param("actor") String actor);
    /** 更新类别的层级、编码、名称、状态及审计字段。 */
    @Update(
            "UPDATE ca_category SET parent_id=#{v.parentId}, category_code=#{v.categoryCode}, " +
            "category_name=#{v.categoryName}, order_num=#{v.orderNum}, status=#{v.status}, remark=#{v.remark}, " +
            "update_by=#{actor}, update_time=NOW(3) WHERE category_id=#{v.categoryId}")
    int updateCategory(@Param("v") CategorySaveDTO value, @Param("actor") String actor);
    /** 按筛选条件和授权部门范围查询场所列表。 */
    List<LocationVO> locations(LocationQueryDTO query);
    /** 按ID顺序读取场所树，供校验层级结构。 */
    @Select(
            "SELECT location_id AS locationId, parent_id AS parentId, location_code AS locationCode, location_name AS " +
            "locationName, location_type AS locationType, dept_id AS deptId, order_num AS orderNum, status, remark FROM " +
            "ca_location ORDER BY location_id ")
    List<LocationVO> selectLocationTree();
    /** 读取指定场所的持久化详情。 */
    @Select(
            "SELECT location_id AS locationId, parent_id AS parentId, location_code AS locationCode, location_name AS " +
            "locationName, location_type AS locationType, dept_id AS deptId, order_num AS orderNum, status, remark FROM " +
            "ca_location WHERE location_id=#{id}")
    LocationVO location(Long id);
    /** 读取指定场所并返回部门及状态。 */
    @Select(
            "SELECT location_id AS locationId, parent_id AS parentId, location_code AS locationCode, location_name AS " +
            "locationName, location_type AS locationType, dept_id AS deptId, order_num AS orderNum, status, remark FROM " +
            "ca_location WHERE location_id=#{id} ")
    LocationVO selectLocationForChange(Long id);
    /** 统计引用指定场所的资产数量。 */
    @Select(
            "SELECT COUNT(1) FROM ca_asset WHERE location_id=#{id}")
    int locationReferences(Long id);
    /** 插入场所及创建审计字段并回填主键。 */
    @Insert(
            "INSERT INTO ca_location(parent_id, location_code, location_name, location_type, dept_id, order_num, status, " +
            "remark, create_by, create_time) VALUES(#{v.parentId}, #{v.locationCode}, #{v.locationName}, " +
            "#{v.locationType}, #{v.deptId}, #{v.orderNum}, #{v.status}, #{v.remark}, #{actor}, NOW(3))")
    @Options(useGeneratedKeys = true, useCache = false, keyProperty = "v.locationId")
    int insertLocation(@Param("v") LocationSaveDTO value, @Param("actor") String actor);
    /** 更新场所层级、部门、状态及审计字段。 */
    @Update(
            "UPDATE ca_location SET parent_id=#{v.parentId}, location_code=#{v.locationCode}, " +
            "location_name=#{v.locationName}, location_type=#{v.locationType}, dept_id=#{v.deptId}, " +
            "order_num=#{v.orderNum}, status=#{v.status}, remark=#{v.remark}, update_by=#{actor}, update_time=NOW(3) " +
            "WHERE location_id=#{v.locationId}")
    int updateLocation(@Param("v") LocationSaveDTO value, @Param("actor") String actor);
    /** 按筛选条件和用户数据范围查询资产台账。 */
    List<AssetVO> assets(AssetQueryDTO query);
    /** 读取指定资产及类别、场所、部门和责任人展示字段。 */
    AssetVO asset(Long id);
    /** 通过稳定二维码定位标识查询资产详情。 */
    AssetVO assetByToken(String token);
    /** 按资产编号查询已使用的资产主键。 */
    @Select(
            "SELECT asset_id FROM ca_asset WHERE asset_code=#{code}")
    Long assetIdByCode(String code);
    /** 新增在库正常资产、稳定二维码标识及审计字段并回填主键。 */
    @Insert(
            "INSERT INTO ca_asset(asset_code, asset_name, category_id, specification, purchase_date, purchase_price, " +
            "supplier_name, expected_use_years, registered_time, dept_id, location_id, keeper_user_id, asset_status, " +
            "condition_status, borrow_enabled, visibility, qr_token, create_by, create_time, remark) " +
            "VALUES(#{v.assetCode}, #{v.assetName}, #{v.categoryId}, #{v.specification}, #{v.purchaseDate}, " +
            "#{v.purchasePrice}, #{v.supplierName}, #{v.expectedUseYears}, NOW(3), #{v.deptId}, #{v.locationId}, " +
            "#{v.keeperUserId}, '0', '0', #{v.borrowEnabled}, #{v.visibility}, #{token}, #{actor}, NOW(3), #{v.remark})")
    @Options(useGeneratedKeys = true, useCache = false, keyProperty = "v.assetId")
    int insertAsset(@Param("v") AssetSaveDTO value, @Param("token") String token, @Param("actor") String actor);
    /** 仅更新在库资产的台账维护字段及修改审计信息。 */
    @Update(
            "UPDATE ca_asset SET asset_name=#{v.assetName}, category_id=#{v.categoryId}, " +
            "specification=#{v.specification}, purchase_date=#{v.purchaseDate}, purchase_price=#{v.purchasePrice}, " +
            "supplier_name=#{v.supplierName}, expected_use_years=#{v.expectedUseYears}, dept_id=#{v.deptId}, " +
            "location_id=#{v.locationId}, keeper_user_id=#{v.keeperUserId}, borrow_enabled=#{v.borrowEnabled}, " +
            "visibility=#{v.visibility}, remark=#{v.remark}, update_by=#{actor}, update_time=NOW(3) WHERE " +
            "asset_id=#{v.assetId} AND asset_status='0'")
    int updateAsset(@Param("v") AssetSaveDTO value, @Param("actor") String actor);
    /** 查询指定资产按时间倒序排列的变更履历。 */
    @Select(
            "SELECT history_id AS historyId, asset_id AS assetId, event_type AS eventType, biz_type AS bizType, biz_id AS " +
            "bizId, before_snapshot AS beforeSnapshot, after_snapshot AS afterSnapshot, operator_user_id AS " +
            "operatorUserId, event_time AS eventTime, change_reason AS changeReason FROM ca_asset_history WHERE " +
            "asset_id=#{id} ORDER BY event_time DESC, history_id DESC")
    List<AssetHistoryVO> history(Long id);
    /** 统计资产尚未结束的借用、维修与报废业务数量。 */
    int pendingBusiness(Long id);
    /** 读取并返回未删除且启用的部门ID。 */
    @Select(
            "SELECT dept_id FROM sys_dept WHERE dept_id=#{id} AND status='0' AND del_flag='0' ")
    Long validDepartment(Long id);
    /** 读取并返回未删除且启用的责任人用户ID。 */
    @Select(
            "SELECT user_id FROM sys_user WHERE user_id=#{id} AND status='0' AND del_flag='0' ")
    Long validUser(Long id);
    /** 普通查询资产当前状态供流程校验；并发写入由后续状态条件更新检查。 */
    @Select(
            "SELECT asset_id AS assetId,asset_code AS assetCode,asset_name AS assetName,category_id AS categoryId, " +
            "dept_id AS deptId,location_id AS locationId,keeper_user_id AS keeperUserId,asset_status AS assetStatus, " +
            "condition_status AS conditionStatus,borrow_enabled AS borrowEnabled,visibility, registered_time AS " +
            "registeredTime,scrapped_time AS scrappedTime,purchase_price AS purchasePrice FROM ca_asset WHERE " +
            "asset_id=#{id} ")
    AssetStateVO selectAssetForChange(@Param("id") Long id);
    /** 查询资产关键字段并生成履历使用的JSON快照。 */
    String snapshot(@Param("id") Long id);
    /** 仅当资产状态符合预期时更新业务状态、完好情况和操作人。 */
    int changeState(@Param("id") Long id, @Param("expected") String expected,
                    @Param("status") String status, @Param("condition") String condition,
                    @Param("operator") String operator);
    /** 写入资产变更前后快照、关联业务、操作者和变更原因。 */
    @Insert(
            "INSERT INTO ca_asset_history(asset_id,event_type,biz_type,biz_id,before_snapshot,after_snapshot, " +
            "operator_user_id,event_time,change_reason) " +
            "VALUES(#{id},#{event},#{bizType},#{bizId},#{before},#{after},#{userId},NOW(3),#{reason})")
    int insertHistory(@Param("id") Long id, @Param("event") String event,
                      @Param("bizType") String bizType, @Param("bizId") Long bizId,
                      @Param("before") String before, @Param("after") String after,
                      @Param("userId") Long userId, @Param("reason") String reason);
}
