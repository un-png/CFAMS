package com.cfams.asset.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.cfams.asset.service.*;

import java.util.*;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;
import com.cfams.common.exception.ServiceException;
import com.cfams.common.utils.SecurityUtils;
import com.cfams.asset.domain.dto.*;
import com.cfams.asset.domain.vo.*;
import com.cfams.asset.domain.dto.AssetQueryDTO;
import com.cfams.asset.domain.dto.CategoryQueryDTO;
import com.cfams.asset.domain.dto.LocationQueryDTO;
import com.cfams.asset.mapper.CampusAssetMapper;
import com.cfams.campus.service.CampusAccessService;
import com.cfams.asset.service.CampusAssetStateService;
import com.cfams.campus.domain.dto.CampusQueryDTO;

/**
 * 资产基础服务。类别、场所树的结构修改串行校验，台账状态只由流程服务修改。
 */
@Service
public class CampusAssetServiceImpl implements CampusAssetService {
    @Autowired
    private CampusAssetMapper campusAssetMapper;
    @Autowired
    private CampusAccessService campusAccessService;
    @Autowired
    private CampusAssetStateService campusAssetStateService;
    @Autowired
    private Validator validator;

    /** 必须在 PageHelper.startPage 之前执行，防止权限查询消耗分页。 */
    @Override
    public void scope(CampusQueryDTO query) {
        campusAccessService.scope(query);
        if (query.getBeginTime() != null && query.getEndTime() != null
                && query.getBeginTime().after(query.getEndTime())) {
            throw new ServiceException("开始时间不能晚于结束时间");
        }
    }

    /** 按关键字和启停状态查询资产类别列表。 */
    @Override
    public List<CategoryVO> categories(CategoryQueryDTO query) {
        return campusAssetMapper.categories(query);
    }
    /** 按筛选条件和授权部门范围查询场所列表。 */
    @Override
    public List<LocationVO> locations(LocationQueryDTO query) {
        return campusAssetMapper.locations(query);
    }
    /** 按筛选条件和用户数据范围查询资产台账。 */
    @Override
    public List<AssetVO> assets(AssetQueryDTO query) {
        return campusAssetMapper.assets(query);
    }
    /** 查询指定资产按时间倒序排列的变更履历。 */
    @Override
    public List<AssetHistoryVO> history(Long id) {
        return campusAssetMapper.history(id);
    }

    /** 读取指定资产类别详情。 */
    @Override
    public CategoryVO category(Long id) {
        return required(campusAssetMapper.category(id), "类别不存在");
    }

    /** 读取指定场所详情并核对部门范围。 */
    @Override
    public LocationVO location(Long id) {
        LocationVO value = required(campusAssetMapper.location(id), "场所不存在");
        requireLocationScope(value.getDeptId());
        return value;
    }

    /** 读取指定资产详情并核对部门及责任人数据权限。 */
    @Override
    public AssetVO asset(Long id) {
        AssetVO value = required(campusAssetMapper.asset(id), "资产不存在");
        campusAccessService.requireData(value.getDeptId(), value.getKeeperUserId());
        return value;
    }

    /** 校验二维码定位标识并返回用户有权查看的资产。 */
    @Override
    public AssetVO resolve(String token) {
        if (token == null || token.isBlank() || token.length() > 64) {
            throw new ServiceException("二维码定位标识无效");
        }
        AssetVO value = required(campusAssetMapper.assetByToken(token), "二维码定位标识无效");
        campusAccessService.requireData(value.getDeptId(), value.getKeeperUserId());
        return value;
    }

    /** 校验资产数量及访问权限，生成去重后的稳定标签数据。 */
    @Override
    public List<AssetLabelVO> labels(List<Long> ids) {
        if (ids == null || ids.isEmpty() || ids.size() > 200) {
            throw new ServiceException("每次请选择1至200件资产");
        }
        List<AssetLabelVO> result = new ArrayList<>();
        for (Long id : new LinkedHashSet<>(ids)) {
            if (id == null || id <= 0) throw new ServiceException("资产ID无效");
            AssetVO value = asset(id);
            result.add(new AssetLabelVO(value.getAssetId(), value.getAssetCode(),
                    value.getAssetName(), value.getQrToken()));
        }
        return result;
    }

    /** 新增或完整更新类别；已有资产引用或启用子节点时不能停用。 */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public CategoryVO saveCategory(CategorySaveDTO value, boolean create) {
        campusAccessService.requirePermission("campus:categories:" + (create ? "add" : "edit"));
        validate(value);
        requireId(value.getCategoryId(), create);
        List<CategoryVO> tree = campusAssetMapper.selectCategoryTree();
        if (!create && tree.stream().noneMatch(v -> v.getCategoryId().equals(value.getCategoryId()))) {
            throw new ServiceException("类别不存在");
        }
        Map<Long, Long> parents = new HashMap<>();
        for (CategoryVO node : tree) {
            parents.put(node.getCategoryId(), node.getParentId());
            if (!Objects.equals(node.getCategoryId(), value.getCategoryId())
                    && node.getCategoryCode().equalsIgnoreCase(value.getCategoryCode())) {
                throw new ServiceException("类别编码已存在");
            }
        }
        checkTree(value.getCategoryId(), value.getParentId(), parents);
        if ("0".equals(value.getStatus()) && value.getParentId() != null) {
            CategoryVO parent = required(campusAssetMapper.category(value.getParentId()), "上级类别不存在");
            if (!"0".equals(parent.getStatus())) throw new ServiceException("上级类别已停用");
        }
        if (!create && "1".equals(value.getStatus())) {
            if (campusAssetMapper.categoryReferences(value.getCategoryId()) > 0
                    || tree.stream().anyMatch(v -> Objects.equals(v.getParentId(), value.getCategoryId())
                    && "0".equals(v.getStatus()))) {
                throw new ServiceException("类别已被资产引用或存在启用子类别，不能停用");
            }
        }
        try {
            if (create) campusAssetMapper.insertCategory(value, SecurityUtils.getUsername());
            else campusAssetMapper.updateCategory(value, SecurityUtils.getUsername());
        } catch (DuplicateKeyException e) {
            throw new ServiceException("类别编码已存在");
        }
        return campusAssetMapper.category(value.getCategoryId());
    }

    /** 场所树与类别树采用相同防环策略，编辑同时校验原部门和新部门范围。 */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public LocationVO saveLocation(LocationSaveDTO value, boolean create) {
        campusAccessService.requirePermission("campus:locations:" + (create ? "add" : "edit"));
        validate(value);
        requireId(value.getLocationId(), create);
        List<LocationVO> tree = campusAssetMapper.selectLocationTree();
        if (!create) location(value.getLocationId());
        requireLocationScope(value.getDeptId());
        if (value.getDeptId() != null) requireValidDepartment(value.getDeptId());
        Map<Long, Long> parents = new HashMap<>();
        for (LocationVO node : tree) {
            parents.put(node.getLocationId(), node.getParentId());
            if (!Objects.equals(node.getLocationId(), value.getLocationId())
                    && node.getLocationCode().equalsIgnoreCase(value.getLocationCode())) {
                throw new ServiceException("场所编码已存在");
            }
        }
        checkTree(value.getLocationId(), value.getParentId(), parents);
        if (value.getParentId() != null) {
            LocationVO parent = required(campusAssetMapper.location(value.getParentId()), "上级场所不存在");
            // 公共上级场所可作为本部门场所的父节点，不授予其编辑权限。
            if (parent.getDeptId() != null) campusAccessService.requireData(parent.getDeptId(), null);
            if ("0".equals(value.getStatus()) && !"0".equals(parent.getStatus())) {
                throw new ServiceException("上级场所已停用");
            }
        }
        if (!create && "1".equals(value.getStatus())
                && (campusAssetMapper.locationReferences(value.getLocationId()) > 0
                || tree.stream().anyMatch(v -> Objects.equals(v.getParentId(), value.getLocationId())
                && "0".equals(v.getStatus())))) {
            throw new ServiceException("场所已被资产引用或存在启用子场所，不能停用");
        }
        try {
            if (create) campusAssetMapper.insertLocation(value, SecurityUtils.getUsername());
            else campusAssetMapper.updateLocation(value, SecurityUtils.getUsername());
        } catch (DuplicateKeyException e) {
            throw new ServiceException("场所编码已存在");
        }
        return campusAssetMapper.location(value.getLocationId());
    }

    /** 新增固定为在库、正常，编号终身不复用。 */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public AssetVO create(AssetSaveDTO value) {
        campusAccessService.requirePermission("campus:assets:add");
        validateNewAsset(value);
        insertValidated(value);
        return campusAssetMapper.asset(value.getAssetId());
    }

    /** 台账更新先读取资产并保存前后快照；禁止借出、维修、报废及未结业务资产修改。 */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public AssetVO update(AssetSaveDTO value) {
        campusAccessService.requirePermission("campus:assets:edit");
        validate(value);
        requireId(value.getAssetId(), false);
        com.cfams.asset.domain.vo.AssetStateVO current = campusAssetStateService.selectForChange(value.getAssetId());
        campusAccessService.requireData(current.getDeptId(), current.getKeeperUserId());
        if (!"0".equals(current.getAssetStatus()) || campusAssetMapper.pendingBusiness(value.getAssetId()) > 0) {
            throw new ServiceException("仅允许修改在库且无未结束借用、维修、报废业务的资产");
        }
        if (!current.getAssetCode().equals(value.getAssetCode())) {
            throw new ServiceException("资产编号入册后不能修改");
        }
        validateReferences(value);
        String before = campusAssetStateService.snapshot(value.getAssetId());
        if (campusAssetMapper.updateAsset(value, SecurityUtils.getUsername()) != 1) {
            throw new ServiceException("资产状态已变化，请刷新后重试");
        }
        campusAssetStateService.history(value.getAssetId(), "EDIT", null, null, before, "PC台账维护");
        return campusAssetMapper.asset(value.getAssetId());
    }

    /** 导入预校验仅供同包事务服务使用，不在校验阶段写入。 */
    @Override
    public void validateNewAsset(AssetSaveDTO value) {
        validate(value);
        requireId(value.getAssetId(), true);
        if (campusAssetMapper.assetIdByCode(value.getAssetCode()) != null) throw new ServiceException("资产编号已存在");
        validateReferences(value);
    }

    /** 必须在已开启事务、完成整批校验后调用。 */
    @Override
    public void insertValidated(AssetSaveDTO value) {
        try {
            campusAssetMapper.insertAsset(value, UUID.randomUUID().toString().replace("-", ""), SecurityUtils.getUsername());
        } catch (DuplicateKeyException e) {
            throw new ServiceException("资产编号已存在，整批操作已撤销");
        }
        campusAssetStateService.history(value.getAssetId(), "REGISTER", null, null, null, "PC资产入册");
    }

    /** 校验并读取资产关联的有效类别、场所、部门与责任人。 */
    private void validateReferences(AssetSaveDTO value) {
        campusAccessService.requireDepartment(value.getDeptId());
        requireValidDepartment(value.getDeptId());
        campusAccessService.requireUser(value.getKeeperUserId());
        required(campusAssetMapper.validUser(value.getKeeperUserId()), "责任人不存在或已停用");
        CategoryVO category = required(campusAssetMapper.selectCategoryForChange(value.getCategoryId()), "类别不存在");
        if (!"0".equals(category.getStatus())) throw new ServiceException("类别已停用");
        LocationVO location = required(campusAssetMapper.selectLocationForChange(value.getLocationId()), "场所不存在");
        if (!"0".equals(location.getStatus())) throw new ServiceException("场所已停用");
        if (location.getDeptId() != null) {
            campusAccessService.requireData(location.getDeptId(), null);
            requireValidDepartment(location.getDeptId());
        }
    }

    /** 确认指定部门存在且未停用或删除。 */
    private void requireValidDepartment(Long id) {
        required(campusAssetMapper.validDepartment(id), "部门不存在或已停用");
    }

    /** 校验场所部门权限，公共场所仅允许全数据范围用户访问。 */
    private void requireLocationScope(Long deptId) {
        if (deptId != null) {
            campusAccessService.requireData(deptId, null);
            return;
        }
        CampusQueryDTO query = new CampusQueryDTO();
        campusAccessService.scope(query);
        if (!query.isUnrestricted()) throw new ServiceException("公共场所仅允许全数据范围管理员维护或查看");
    }

    /** 执行资产维护参数约束校验并报告首个字段错误。 */
    private void validate(Object value) {
        Set<jakarta.validation.ConstraintViolation<Object>> violations = validator.validate(value);
        if (!violations.isEmpty()) {
            jakarta.validation.ConstraintViolation<Object> first = violations.iterator().next();
            throw new ServiceException(first.getPropertyPath() + ": " + first.getMessage());
        }
    }

    /** 区分新增和修改请求并校验主键是否符合要求。 */
    private static void requireId(Long id, boolean create) {
        if (create && id != null) throw new ServiceException("新增请求不能指定ID");
        if (!create && (id == null || id <= 0)) throw new ServiceException("修改请求必须提供有效ID");
    }

    /** 拒绝不存在的查询结果并抛出指定业务错误。 */
    private static <T> T required(T value, String message) {
        if (value == null) throw new ServiceException(message);
        return value;
    }

    /** 包含历史异常环的检测，禁止挂到自身或任何后代下。 */
    static void checkTree(Long id, Long parentId, Map<Long, Long> parents) {
        Set<Long> visited = new HashSet<>();
        if (id != null) visited.add(id);
        Long cursor = parentId;
        while (cursor != null) {
            if (!visited.add(cursor)) throw new ServiceException("上级节点形成循环引用");
            if (!parents.containsKey(cursor)) throw new ServiceException("上级节点不存在");
            cursor = parents.get(cursor);
        }
    }
}
