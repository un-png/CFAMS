package com.cfams.inventory.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cfams.campus.service.CampusAccessService;
import com.cfams.asset.service.CampusAssetStateService;
import com.cfams.asset.domain.vo.AssetStateVO;
import com.cfams.campus.domain.dto.CampusQueryDTO;
import com.cfams.common.exception.ServiceException;
import com.cfams.common.utils.SecurityUtils;
import com.cfams.inventory.domain.InventoryRepair;
import com.cfams.inventory.domain.dto.*;
import com.cfams.inventory.domain.vo.*;
import com.cfams.inventory.mapper.CampusInventoryMapper;
import com.cfams.inventory.service.CampusInventoryService;
import com.cfams.message.service.CampusMessageService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 盘点事务按资产、任务、明细顺序读取并校验；现场事实不由PC构造。
 */
@Service
public class CampusInventoryServiceImpl implements CampusInventoryService {

    @Autowired
    private CampusInventoryMapper campusInventoryMapper;

    @Autowired
    private CampusAccessService campusAccessService;

    @Autowired
    private CampusAssetStateService campusAssetStateService;

    @Autowired
    private CampusMessageService campusMessageService;

    /**
     * 按查询条件和数据权限查询盘点任务列表。
     */
    @Override
    public List<InventoryTaskVO> list(InventoryTaskQueryDTO query) {
        campusAccessService.scope(query);
        return campusInventoryMapper.list(query);
    }

    /**
     * 查询盘点任务详情，并校验当前用户的数据访问范围。
     */
    @Override
    public InventoryTaskVO detail(Long id) {
        InventoryTaskVO task = required(campusInventoryMapper.detail(id));
        campusAccessService.requireData(task.getDeptId(), task.getPublisherUserId());
        task.setUserIds(campusInventoryMapper.users(id));
        return task;
    }

    /**
     * 校验草稿范围与执行人后创建盘点任务。
     */
    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Long create(InventoryTaskSaveDTO request) {
        campusAccessService.requirePermission("campus:inventory:add");
        validateDraft(request);
        InventoryTaskVO task = draft(request);
        task.setTaskNo("IN" + UUID.randomUUID().toString().replace("-", ""));
        campusInventoryMapper.insertTask(task);
        assignUsers(task.getTaskId(), request.getUserIds());
        return task.getTaskId();
    }

    /**
     * 修改尚未发布的任务草稿及执行人分配。
     */
    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void edit(Long id, InventoryTaskSaveDTO request) {
        campusAccessService.requirePermission("campus:inventory:add");
        InventoryTaskVO existing = required(campusInventoryMapper.selectTaskForChange(id));
        campusAccessService.requireDepartment(existing.getDeptId());
        requireDraft(existing);
        validateDraft(request);
        InventoryTaskVO task = draft(request);
        task.setTaskId(id);
        changed(campusInventoryMapper.editTask(task));
        campusInventoryMapper.deleteUsers(id);
        assignUsers(id, request.getUserIds());
    }

    /**
     * 校验草稿的时间、范围及执行人有效性。
     */
    private void validateDraft(InventoryTaskSaveDTO request) {
        campusAccessService.requireDepartment(request.getDeptId());
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new ServiceException("截止时间必须晚于开始时间");
        }
        if (request.getScopeDeptId() != null) {
            campusAccessService.requireDepartment(request.getScopeDeptId());
        }
        if (campusInventoryMapper.validScope(
                        request.getScopeDeptId(),
                        request.getScopeLocationId(),
                        request.getScopeCategoryId())
                != 1) {
            throw new ServiceException("盘点范围中的部门、场所或类别不存在或已停用");
        }
        if (campusInventoryMapper.validScope(request.getDeptId(), null, null) != 1) {
            throw new ServiceException("任务管理部门无效");
        }
        if (new HashSet<>(request.getUserIds()).size() != request.getUserIds().size()) {
            throw new ServiceException("执行人不能重复");
        }
        request.getUserIds().forEach(this::requireExecutor);
    }

    /**
     * 将请求白名单字段转换为盘点任务草稿。
     */
    private InventoryTaskVO draft(InventoryTaskSaveDTO request) {
        InventoryTaskVO task = new InventoryTaskVO();
        task.setTaskName(request.getTaskName());
        task.setDeptId(request.getDeptId());
        task.setScopeDeptId(request.getScopeDeptId());
        task.setScopeLocationId(request.getScopeLocationId());
        task.setScopeCategoryId(request.getScopeCategoryId());
        task.setStartTime(request.getStartTime());
        task.setEndTime(request.getEndTime());
        task.setRemark(request.getRemark());
        task.setCreateBy(SecurityUtils.getUsername());
        return task;
    }

    /**
     * 按用户主键升序保存任务执行人关系。
     */
    private void assignUsers(Long id, List<Long> users) {
        for (Long user : users.stream().sorted().collect(Collectors.toList())) {
            com.cfams.common.entity.CaInventoryUser member =
                    new com.cfams.common.entity.CaInventoryUser();
            member.setTaskId(id);
            member.setUserId(user);
            member.setAssignedBy(SecurityUtils.getUserId());
            campusInventoryMapper.insertUser(member);
        }
    }

    /**
     * 校验用户有效且具备盘点执行权限。
     */
    private void requireExecutor(Long id) {
        campusAccessService.requireUser(id);
        if (campusInventoryMapper.qualifiedUser(id) == 0) {
            throw new ServiceException("执行人未获得盘点执行权限：" + id);
        }
    }

    /**
     * 查询草稿范围内可分配执行人的资产清单。
     */
    @Override
    public List<InventoryAssetVO> candidates(Long id) {
        InventoryTaskVO task = detail(id);
        requireDraft(task);
        CampusQueryDTO query = new CampusQueryDTO();
        campusAccessService.scope(query);
        return campusInventoryMapper.candidates(task, query);
    }

    /**
     * 校验完整分配清单并按资产顺序查询，发布任务并冻结盘点快照。
     */
    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void publish(Long id, InventoryPublishDTO request) {
        campusAccessService.requirePermission("campus:inventory:publish");
        InventoryTaskVO initial = detail(id);
        requireDraft(initial);
        CampusQueryDTO query = new CampusQueryDTO();
        campusAccessService.scope(query);
        List<InventoryAssetVO> candidates = campusInventoryMapper.candidates(initial, query);
        if (candidates.isEmpty()) {
            throw new ServiceException("范围内没有可盘点资产，不能发布空任务");
        }
        if (candidates.size() > 10000) {
            throw new ServiceException("单任务最多10000件资产，请缩小范围");
        }
        Map<Long, Long> assignments = new HashMap<>();
        for (InventoryPublishDTO.Assignment assignment : request.getAssignments()) {
            if (assignments.put(assignment.getAssetId(), assignment.getUserId()) != null) {
                throw new ServiceException("资产不能重复分配");
            }
        }
        Set<Long> ids = candidates.stream().map(a -> a.getAssetId()).collect(Collectors.toSet());
        if (!ids.equals(assignments.keySet())) {
            throw new ServiceException("分配清单必须完整覆盖当前范围，请刷新范围预览");
        }
        // 任何任务普通查询之前按资产主键升序读取，防止与借还、维修、报废逆序。
        for (InventoryAssetVO candidate :
                candidates.stream()
                        .sorted(Comparator.comparing(a -> a.getAssetId()))
                        .collect(Collectors.toList())) {
            AssetStateVO current = campusAssetStateService.selectForChange(candidate.getAssetId());
            campusAccessService.requireDepartment(current.getDeptId());
            if ("3".equals(current.getAssetStatus())
                    || !Objects.equals(current.getDeptId(), candidate.getDeptId())
                    || !Objects.equals(current.getLocationId(), candidate.getLocationId())
                    || !Objects.equals(current.getCategoryId(), candidate.getCategoryId())) {
                throw new ServiceException("资产范围已变化，请重新预览后发布");
            }
        }
        InventoryTaskVO task = required(campusInventoryMapper.selectTaskForChange(id));
        requireDraft(task);
        campusAccessService.requireDepartment(task.getDeptId());
        if (!sameDraft(initial, task)) {
            throw new ServiceException("任务草稿已修改，请刷新后发布");
        }
        Set<Long> currentIds =
                campusInventoryMapper.candidates(task, query).stream()
                        .map(a -> a.getAssetId())
                        .collect(Collectors.toSet());
        if (!ids.equals(currentIds)) {
            throw new ServiceException("资产范围已变化，请重新预览后发布");
        }
        List<Long> users = campusInventoryMapper.users(id);
        if (!new HashSet<>(users).equals(new HashSet<>(initial.getUserIds()))) {
            throw new ServiceException("任务执行人已修改，请刷新");
        }
        users.forEach(this::requireExecutor);
        if (!users.containsAll(assignments.values())) {
            throw new ServiceException("明细执行人必须属于任务人员");
        }
        changed(campusInventoryMapper.publish(id, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
        for (InventoryAssetVO candidate : candidates) {
            InventoryItemVO item = new InventoryItemVO();
            item.setTaskId(id);
            item.setAssetId(candidate.getAssetId());
            item.setAssignedUserId(assignments.get(candidate.getAssetId()));
            item.setBaseSnapshot(campusAssetStateService.snapshot(candidate.getAssetId()));
            item.setCreateBy(SecurityUtils.getUsername());
            campusInventoryMapper.insertItem(item);
        }
        for (Long user : new HashSet<>(assignments.values())) {
            campusMessageService.notify(
                    user,
                    "inventory:" + id + ":published",
                    "INVENTORY",
                    "INVENTORY_TASK",
                    id,
                    "盘点任务已发布",
                    "请按任务范围完成盘点：" + task.getTaskName());
        }
    }

    /**
     * 比较任务范围、计划时间及更新时间，检测发布前的并发修改。
     */
    private boolean sameDraft(InventoryTaskVO a, InventoryTaskVO b) {
        return Objects.equals(a.getTaskName(), b.getTaskName())
                && Objects.equals(a.getDeptId(), b.getDeptId())
                && Objects.equals(a.getScopeDeptId(), b.getScopeDeptId())
                && Objects.equals(a.getScopeLocationId(), b.getScopeLocationId())
                && Objects.equals(a.getScopeCategoryId(), b.getScopeCategoryId())
                && Objects.equals(a.getStartTime(), b.getStartTime())
                && Objects.equals(a.getEndTime(), b.getEndTime())
                && Objects.equals(a.getUpdateTime(), b.getUpdateTime());
    }

    /**
     * 校验计划开始时间后将已发布任务转入执行中。
     */
    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void start(Long id) {
        campusAccessService.requirePermission("campus:inventory:publish");
        InventoryTaskVO task = managedTask(id, "0");
        if (LocalDateTime.now().isBefore(task.getStartTime())) {
            throw new ServiceException("尚未到计划开始时间");
        }
        transition(id, "0", "1");
    }

    /**
     * 将执行中的盘点任务提交复核。
     */
    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void submit(Long id) {
        campusAccessService.requirePermission("campus:inventory:publish");
        managedTask(id, "1");
        transition(id, "1", "2");
    }

    /**
     * 确认全部明细有复核结论后结项并通知发布人。
     */
    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void complete(Long id) {
        campusAccessService.requirePermission("campus:inventoryReview:review");
        InventoryTaskVO task = managedTask(id, "2");
        if (campusInventoryMapper.unreviewed(id) != 0) {
            throw new ServiceException("每条明细均需有复核结论后才能结项");
        }
        transition(id, "2", "3");
        campusMessageService.notify(
                task.getPublisherUserId(),
                "inventory:" + id + ":completed",
                "INVENTORY",
                "INVENTORY_TASK",
                id,
                "盘点任务已结项",
                "盘点任务已完成复核：" + task.getTaskName());
    }

    /**
     * 读取已发布任务并校验管理部门权限和预期状态。
     */
    private InventoryTaskVO managedTask(Long id, String expected) {
        InventoryTaskVO task = required(campusInventoryMapper.selectTaskForChange(id));
        campusAccessService.requireDepartment(task.getDeptId());
        if (task.getPublishTime() == null || !expected.equals(task.getStatus())) {
            throw new ServiceException("任务状态不允许该操作");
        }
        return task;
    }

    /**
     * 按预期状态执行状态迁移，并检查受影响行数。
     */
    private void transition(Long id, String from, String to) {
        changed(
                campusInventoryMapper.transition(
                        id, from, to, SecurityUtils.getUserId(), SecurityUtils.getUsername()));
    }

    /**
     * 按查询条件和数据范围查询盘点明细。
     */
    @Override
    public List<InventoryItemVO> items(InventoryItemQueryDTO query) {
        campusAccessService.scope(query);
        return campusInventoryMapper.items(query);
    }

    /**
     * 查询单条盘点明细并校验所属任务的访问权限。
     */
    @Override
    public InventoryItemVO item(Long id) {
        InventoryItemVO item = requiredItem(campusInventoryMapper.item(id));
        detail(item.getTaskId());
        return item;
    }

    /**
     * 复核盘点差异，按结论退回重盘或更新台账及维修关联。
     */
    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void review(Long id, InventoryReviewDTO request) {
        campusAccessService.requirePermission("campus:inventoryReview:review");
        InventoryItemVO initial = requiredItem(campusInventoryMapper.item(id));
        AssetStateVO asset = campusAssetStateService.selectForChange(initial.getAssetId());
        managedTask(initial.getTaskId(), "2");
        InventoryItemVO item = requiredItem(campusInventoryMapper.selectItemForChange(id));
        if ("1".equals(item.getReviewStatus())) {
            throw new ServiceException("明细已复核，不允许重复办理");
        }
        if (!Objects.equals(item.getAssetId(), initial.getAssetId())
                || !Objects.equals(item.getTaskId(), initial.getTaskId())) {
            throw new ServiceException("盘点明细关联已变化");
        }
        item.setReviewSnapshot(campusAssetStateService.snapshot(item.getAssetId()));
        item.setReviewNote(request.getReviewNote());
        item.setReviewUserId(SecurityUtils.getUserId());
        item.setCreateBy(SecurityUtils.getUsername());
        if (!request.getApproved()) {
            if (request.getResolvedLocationId() != null
                    || request.getResolvedCondition() != null
                    || request.getRepairId() != null
                    || Boolean.TRUE.equals(request.getCreateRepair())) {
                throw new ServiceException("退回重盘不得同时修改台账或创建维修");
            }
            item.setReviewStatus("2");
            changed(campusInventoryMapper.review(item));
            transition(item.getTaskId(), "2", "1");
            // 退回事件用新生成时间作为独立事件，不复用发布消息的去重键。
            campusMessageService.notify(
                    item.getAssignedUserId(),
                    "inventory_item:" + id + ":returned:" + UUID.randomUUID(),
                    "INVENTORY",
                    "INVENTORY_TASK",
                    item.getTaskId(),
                    "盘点明细退回重盘",
                    request.getReviewNote());
            return;
        }
        approveItem(item, asset, request);
        item.setReviewStatus("1");
        changed(campusInventoryMapper.review(item));
    }

    /**
     * 处理通过复核的明细，校验现场异常与台账变化并应用明确结论。
     */
    private void approveItem(
            InventoryItemVO item, AssetStateVO asset, InventoryReviewDTO request) {
        boolean repairRequested =
                request.getRepairId() != null || Boolean.TRUE.equals(request.getCreateRepair());
        boolean applyChanges =
                request.getResolvedLocationId() != null
                        || request.getResolvedCondition() != null
                        || repairRequested;
        if (item.getCheckedTime() == null) {
            if (applyChanges) {
                throw new ServiceException("未盘明细只能记录未盘结论，不得更改台账");
            }
            item.setReviewNote("本次未完成盘点，不改台账；" + request.getReviewNote());
            if (item.getReviewNote().length() > 1000) {
                throw new ServiceException("复核说明过长");
            }
            return;
        }
        if (Boolean.TRUE.equals(request.getRecordOnly())) {
            if (applyChanges) {
                throw new ServiceException("仅记录结论时不得同时调整台账或关联维修");
            }
            if (!"3".equals(asset.getAssetStatus()) && !ledgerChanged(item)) {
                throw new ServiceException("台账未变化的异常明细应明确处理，不能仅记录结论");
            }
            return;
        }
        if (!applyChanges) {
            // 无台账变化的普通确认可保留；异常必须明确处理，不能静默跳过。
            if (!"3".equals(asset.getAssetStatus())
                    && ("1".equals(item.getMissingFlag())
                            || "1".equals(item.getObservedCondition())
                            || "2".equals(item.getObservedCondition())
                            || !Objects.equals(
                                    item.getObservedLocationId(), asset.getLocationId()))) {
                throw new ServiceException("异常明细必须明确复核位置、完好状态或关联维修");
            }
            return;
        }
        campusAccessService.requireDepartment(asset.getDeptId());
        if ("3".equals(asset.getAssetStatus())) {
            throw new ServiceException("已注销资产只可记录结论，不能更改台账");
        }
        requireUnchanged(item);
        if (request.getResolvedLocationId() != null
                && campusInventoryMapper.validScope(null, request.getResolvedLocationId(), null) != 1) {
            throw new ServiceException("复核位置不存在或已停用");
        }
        if ("1".equals(item.getMissingFlag())) {
            if (!"2".equals(request.getResolvedCondition())
                    || request.getResolvedLocationId() != null
                    || repairRequested) {
                throw new ServiceException("疑似丢失只能置为待核验，不能直接注销、变更位置或报修");
            }
        }
        String condition =
                request.getResolvedCondition() == null
                        ? asset.getConditionStatus()
                        : request.getResolvedCondition();
        if ("1".equals(item.getObservedCondition()) && !"1".equals(condition)) {
            throw new ServiceException("损坏盘点结果需确认损坏并关联报修，不得直接恢复正常");
        }
        if ("2".equals(item.getObservedCondition()) && !"2".equals(condition)) {
            throw new ServiceException("待核验的现场结果不能通过普通盘点直接认定正常");
        }
        if ("1".equals(condition)) {
            item.setRepairId(resolveRepair(item, asset, request));
        } else if (repairRequested) {
            throw new ServiceException("仅损坏资产可在此关联或创建报修");
        }
        if ("0".equals(condition) && !"0".equals(asset.getConditionStatus())) {
            throw new ServiceException("已有损坏或待核验状态不能通过普通盘点直接恢复正常");
        }
        if (campusInventoryMapper.pendingScrap(item.getAssetId()) > 0) {
            throw new ServiceException("资产正在报废审核，不能调整台账");
        }
        changed(
                campusInventoryMapper.updateAsset(
                        item.getAssetId(),
                        request.getResolvedLocationId(),
                        request.getResolvedCondition(),
                        SecurityUtils.getUsername()));
        item.setResolvedLocationId(request.getResolvedLocationId());
        item.setResolvedCondition(request.getResolvedCondition());
        campusAssetStateService.history(
                item.getAssetId(),
                "INVENTORY",
                "INVENTORY_ITEM",
                item.getItemId(),
                item.getReviewSnapshot(),
                request.getReviewNote());
    }

    /**
     * 校验发布后的资产快照及履历未变化，防止覆盖新台账。
     */
    private void requireUnchanged(InventoryItemVO item) {
        if (ledgerChanged(item)) {
            throw new ServiceException("发布后台账或履历已变化，请人工重核，不得按旧快照覆盖");
        }
    }

    /**
     * 比较发布与复核快照，并查询发布后是否存在资产履历变化。
     */
    private boolean ledgerChanged(InventoryItemVO item) {
        JSONObject base = JSON.parseObject(item.getBaseSnapshot());
        JSONObject current = JSON.parseObject(item.getReviewSnapshot());
        for (String key :
                List.of(
                        "dept_id",
                        "location_id",
                        "keeper_user_id",
                        "category_id",
                        "asset_status",
                        "condition_status",
                        "borrow_enabled",
                        "visibility")) {
            if (!Objects.equals(base.getString(key), current.getString(key))) {
                return true;
            }
        }
        return campusInventoryMapper.changedSincePublish(item.getAssetId(), item.getTaskId()) > 0;
    }

    /**
     * 校验已有维修关联或按复核信息创建真实报修单。
     */
    private Long resolveRepair(
            InventoryItemVO item, AssetStateVO asset, InventoryReviewDTO request) {
        List<Long> active = campusInventoryMapper.activeRepairs(item.getAssetId());
        if (request.getRepairId() != null) {
            if (Boolean.TRUE.equals(request.getCreateRepair())) {
                throw new ServiceException("不能同时选择已有维修和新建维修");
            }
            if (!active.contains(request.getRepairId())) {
                throw new ServiceException("关联维修必须属于本资产且尚未完成");
            }
            return request.getRepairId();
        }
        if (!active.isEmpty()) {
            throw new ServiceException("已有未完成报修，请指定关联该报修单");
        }
        if (!Boolean.TRUE.equals(request.getCreateRepair())) {
            throw new ServiceException("损坏资产必须关联已有报修或创建报修");
        }
        if (request.getFaultType() == null
                || campusInventoryMapper.validFault(request.getFaultType()) == 0
                || request.getFaultDescription() == null
                || request.getFaultDescription().isBlank()
                || request.getFaultLocation() == null
                || request.getFaultLocation().isBlank()) {
            throw new ServiceException("新建报修需有效故障类型、故障描述和现场地点");
        }
        InventoryRepair repair = new InventoryRepair();
        repair.setRepairNo("RP" + UUID.randomUUID().toString().replace("-", ""));
        repair.setAssetId(item.getAssetId());
        repair.setAssetDeptId(asset.getDeptId());
        repair.setReporterUserId(SecurityUtils.getUserId());
        repair.setFaultType(request.getFaultType());
        repair.setFaultDescription(request.getFaultDescription());
        repair.setFaultLocation(request.getFaultLocation());
        repair.setFaultTime(item.getCheckedTime());
        repair.setCreateBy(SecurityUtils.getUsername());
        campusInventoryMapper.insertRepair(repair);
        campusMessageService.notify(
                asset.getKeeperUserId(),
                "inventory_item:" + item.getItemId() + ":repair:" + repair.getRepairId(),
                "REPAIR",
                "REPAIR",
                repair.getRepairId(),
                "盘点发现损坏已报修",
                "资产" + asset.getAssetCode() + "已生成待受理报修");
        return repair.getRepairId();
    }

    /**
     * 校验盘点任务存在并返回任务。
     */
    private InventoryTaskVO required(InventoryTaskVO task) {
        if (task == null) {
            throw new ServiceException("盘点任务不存在");
        }
        return task;
    }

    /**
     * 校验盘点明细存在并返回明细。
     */
    private InventoryItemVO requiredItem(InventoryItemVO item) {
        if (item == null) {
            throw new ServiceException("盘点明细不存在");
        }
        return item;
    }

    /**
     * 校验任务处于未发布的草稿状态。
     */
    private void requireDraft(InventoryTaskVO task) {
        if (!"0".equals(task.getStatus()) || task.getPublishTime() != null) {
            throw new ServiceException("仅未发布任务可修改或发布");
        }
    }

    /**
     * 校验操作恰好影响一行，失败时提示刷新后重试。
     */
    private void changed(int count) {
        if (count != 1) {
            throw new ServiceException("数据状态已变化，请刷新后重试");
        }
    }
}
