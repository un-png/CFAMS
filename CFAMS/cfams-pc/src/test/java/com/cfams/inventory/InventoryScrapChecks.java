package com.cfams.inventory;

import com.cfams.campus.service.CampusAccessService;
import com.cfams.asset.service.CampusAssetStateService;
import com.cfams.asset.domain.vo.AssetStateVO;
import com.cfams.campus.domain.dto.CampusQueryDTO;
import com.cfams.common.core.domain.entity.SysUser;
import com.cfams.common.core.domain.model.LoginUser;
import com.cfams.common.exception.ServiceException;
import com.cfams.inventory.domain.dto.*;
import com.cfams.inventory.domain.vo.*;
import com.cfams.inventory.mapper.CampusInventoryMapper;
import com.cfams.inventory.service.impl.CampusInventoryServiceImpl;
import com.cfams.message.service.CampusMessageService;
import com.cfams.message.service.impl.CampusMessageServiceImpl;
import com.cfams.scrap.domain.dto.*;
import com.cfams.scrap.domain.vo.*;
import com.cfams.scrap.mapper.CampusScrapMapper;
import com.cfams.scrap.service.impl.CampusScrapServiceImpl;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 无额外测试依赖的业务边界回归；不会连接数据库或发送真实通知。
 */
public final class InventoryScrapChecks {

    private static int assertions;

    private static final String SNAPSHOT =
            "{\"dept_id\":\"10\",\"location_id\":\"20\",\"keeper_user_id\":\"30\","
                + "\"category_id\":\"40\",\"asset_status\":\"0\",\"condition_status\":\"0\",\"borrow_enabled\":\"1\",\"visibility\":\"1\"}";

    /**
     * 运行不连接数据库的盘点与报废业务边界回归。
     */
    public static void main(String[] args) throws Exception {
        login(200L);
        parseXml();
        InventoryBeanChecks.verify();
        scrapCases();
        inventoryCases();
        publicationCases();
        SecurityContextHolder.clearContext();
        System.out.println("InventoryScrapChecks passed: " + assertions + " assertions");
    }

    /**
     * 解析迁移后的模块映射文件并验证数据范围及递归范围查询。
     */
    private static void parseXml() throws Exception {
        Configuration configuration = new Configuration();
        for (String resource :
                List.of(
                        "mapper/pc/inventory/CampusInventoryMapper.xml",
                        "mapper/pc/scrap/CampusScrapMapper.xml")) {
            try (InputStream stream =
                    InventoryScrapChecks.class.getClassLoader().getResourceAsStream(resource)) {
                check(stream != null, "XML resource exists");
                new XMLMapperBuilder(
                                stream, configuration, resource, configuration.getSqlFragments())
                        .parse();
            }
        }
        InventoryItemQueryDTO query = new InventoryItemQueryDTO();
        query.setAllowedDeptIds(List.of(10L));
        query.setSelfUserId(200L);
        query.setKeyword("asset");
        query.setReviewStatus("0");
        for (String method : List.of("list", "items")) {
            String sql =
                    configuration
                            .getMappedStatement(
                                    CampusInventoryMapper.class.getName() + "." + method)
                            .getBoundSql(query)
                            .getSql();
            check(
                    sql.contains("publisher_user_id") && sql.contains("IN"),
                    "inventory SQL data scope");
        }
        InventoryTaskVO task = new InventoryTaskVO();
        task.setScopeLocationId(20L);
        task.setScopeCategoryId(40L);
        String sql =
                configuration
                        .getMappedStatement(CampusInventoryMapper.class.getName() + ".candidates")
                        .getBoundSql(Map.of("task", task, "q", query))
                        .getSql();
        check(sql.contains("WITH RECURSIVE"), "scope includes descendant locations/categories");
    }

    /**
     * 验证处置证明、业务冲突、审核人隔离和终审资产联动。
     */
    private static void scrapCases() {
        Fixture f = new Fixture();
        ScrapVO scrap = new ScrapVO();
        scrap.setScrapId(1L);
        scrap.setAssetId(1L);
        scrap.setAssetDeptId(10L);
        scrap.setApplicantUserId(100L);
        scrap.setScrapNo("SC-test");
        scrap.setStatus("0");
        Map<String, Object> values = new HashMap<>();
        values.put("detail", scrap);
        values.put("selectForChange", scrap);
        values.put("selectProofIds", List.of());
        values.put("audits", List.of());
        CampusScrapMapper mapper = proxy(CampusScrapMapper.class, values, f.events);
        CampusScrapServiceImpl service =
                com.cfams.campus.TestInjection.inject(new CampusScrapServiceImpl(), "campusScrapMapper", mapper, "campusAccessService", f.access, "campusAssetStateService", f.assets, "campusMessageService", f.messages);
        expect("证明", () -> service.audit(1L, "1", new ScrapAuditDTO(true, "ok")));
        check(f.events.indexOf("asset:1") < f.events.indexOf("selectForChange"), "asset before scrap selectForChange");
        login(100L);
        expect("申请人", () -> service.audit(1L, "1", new ScrapAuditDTO(true, "ok")));
        login(200L);
        values.put("selectProofIds", List.of(9L));
        values.put("unfinishedBorrow", 1);
        expect("借还", () -> service.audit(1L, "1", new ScrapAuditDTO(true, "ok")));
        values.put("transition", 1);
        values.put("selectProofIds", List.of());
        f.events.clear();
        service.audit(1L, "1", new ScrapAuditDTO(false, "材料不全且业务冲突"));
        check(
                !f.events.contains("selectProofIds")
                        && !f.events.contains("unfinishedBorrow")
                        && !f.events.contains("changeState"),
                "rejection works despite missing proofs and open business");
        values.put("selectProofIds", List.of(9L));
        values.put("unfinishedBorrow", 0);
        values.put("unfinishedRepair", 1);
        expect("维修", () -> service.audit(1L, "1", new ScrapAuditDTO(true, "ok")));
        values.put("unfinishedRepair", 0);
        values.put("transition", 1);
        f.events.clear();
        service.audit(1L, "1", new ScrapAuditDTO(true, "ok"));
        check(!f.events.contains("changeState"), "first audit never scraps asset");
        scrap.setStatus("1");
        ScrapAuditVO first = new ScrapAuditVO();
        first.setAuditLevel("1");
        first.setAuditResult("1");
        first.setAuditorUserId(200L);
        values.put("audits", List.of(first));
        expect("不同人员", () -> service.audit(1L, "2", new ScrapAuditDTO(true, "ok")));
        login(300L);
        values.put("selectProofIds", List.of());
        expect("证明", () -> service.audit(1L, "2", new ScrapAuditDTO(true, "ok")));
        values.put("selectProofIds", List.of(9L));
        f.events.clear();
        service.audit(1L, "2", new ScrapAuditDTO(true, "ok"));
        check(
                f.events.containsAll(List.of("changeState", "history", "notify")),
                "final audit updates ledger/history/message");
        login(200L);
    }

    /**
     * 验证未盘、损坏、疑似丢失、台账变化、退回及结项边界。
     */
    private static void inventoryCases() {
        Fixture f = new Fixture();
        InventoryTaskVO task = new InventoryTaskVO();
        task.setTaskId(1L);
        task.setDeptId(10L);
        task.setPublisherUserId(100L);
        task.setPublishTime(LocalDateTime.now().minusDays(1));
        task.setStartTime(task.getPublishTime());
        task.setStatus("2");
        InventoryItemVO item = new InventoryItemVO();
        item.setItemId(1L);
        item.setTaskId(1L);
        item.setAssetId(1L);
        item.setAssignedUserId(100L);
        item.setReviewStatus("0");
        item.setBaseSnapshot(SNAPSHOT);
        item.setMissingFlag("0");
        Map<String, Object> values = new HashMap<>();
        values.put("item", item);
        values.put("selectItemForChange", item);
        values.put("selectTaskForChange", task);
        values.put("review", 1);
        values.put("transition", 1);
        values.put("validScope", 1);
        values.put("updateAsset", 1);
        values.put("activeRepairs", List.of());
        CampusInventoryMapper mapper = proxy(CampusInventoryMapper.class, values, f.events);
        CampusInventoryServiceImpl service =
                com.cfams.campus.TestInjection.inject(new CampusInventoryServiceImpl(), "campusInventoryMapper", mapper, "campusAccessService", f.access, "campusAssetStateService", f.assets, "campusMessageService", f.messages);
        expect("未盘", () -> service.review(1L, review(true, false, 20L, null, null, false)));
        service.review(1L, review(true, false, null, null, null, false));
        check(
                item.getReviewNote().startsWith("本次未完成盘点"),
                "unchecked item gets explicit no-ledger conclusion");
        item.setReviewStatus("0");
        item.setCheckedTime(LocalDateTime.now());
        item.setObservedLocationId(20L);
        item.setObservedCondition("1");
        expect("异常", () -> service.review(1L, review(true, false, null, null, null, false)));
        expect("损坏", () -> service.review(1L, review(true, false, null, "0", null, false)));
        expect("报修", () -> service.review(1L, review(true, false, null, "1", null, false)));
        values.put("activeRepairs", List.of(9L));
        service.review(1L, review(true, false, null, "1", 9L, false));
        check(Objects.equals(item.getRepairId(), 9L), "damage linked to genuine active repair");
        item.setReviewStatus("0");
        item.setMissingFlag("1");
        item.setObservedCondition("2");
        expect("疑似丢失", () -> service.review(1L, review(true, false, null, "0", null, false)));
        f.events.clear();
        service.review(1L, review(true, false, null, "2", null, false));
        check(!f.events.contains("changeState"), "missing inventory does not scrap asset");
        item.setReviewStatus("0");
        values.put("changedSincePublish", 1);
        expect("台账", () -> service.review(1L, review(true, false, null, "2", null, false)));
        service.review(1L, review(true, true, null, null, null, false));
        check(
                "1".equals(item.getReviewStatus()),
                "changed ledger can record explicit no-change conclusion");
        item.setReviewStatus("0");
        f.events.clear();
        service.review(1L, review(false, false, null, null, null, false));
        check("2".equals(item.getReviewStatus()), "return for recheck");
        check(
                f.events.indexOf("asset:1") < f.events.indexOf("selectTaskForChange")
                        && f.events.indexOf("selectTaskForChange") < f.events.indexOf("selectItemForChange"),
                "asset/task/item selectForChange order");
        values.put("unreviewed", 1);
        expect("每条", () -> service.complete(1L));
        values.put("unreviewed", 0);
        service.complete(1L);
        assertions++;
    }

    /**
     * 按指定测试条件构造复核请求，保留原有全参数构造顺序。
     */
    private static InventoryReviewDTO review(
            boolean approved,
            boolean recordOnly,
            Long location,
            String condition,
            Long repair,
            boolean createRepair) {
        return new InventoryReviewDTO(
                approved,
                "已核实现场情况",
                recordOnly,
                location,
                condition,
                repair,
                createRepair,
                null,
                null,
                null);
    }

    /**
     * 验证分配清单完整性、执行人权限和发布锁顺序。
     */
    private static void publicationCases() {
        Fixture f = new Fixture();
        InventoryTaskVO task = new InventoryTaskVO();
        task.setTaskId(1L);
        task.setDeptId(10L);
        task.setStatus("0");
        task.setTaskName("test");
        List<InventoryAssetVO> candidates = new ArrayList<>();
        for (long id : new long[] {2L, 1L}) {
            InventoryAssetVO asset = new InventoryAssetVO();
            asset.setAssetId(id);
            asset.setDeptId(10L);
            asset.setLocationId(20L);
            asset.setCategoryId(40L);
            candidates.add(asset);
        }
        Map<String, Object> values = new HashMap<>();
        values.put("detail", task);
        values.put("selectTaskForChange", task);
        values.put("candidates", candidates);
        values.put("users", List.of(100L));
        values.put("qualifiedUser", 1);
        values.put("publish", 1);
        CampusInventoryMapper mapper = proxy(CampusInventoryMapper.class, values, f.events);
        CampusInventoryServiceImpl service =
                com.cfams.campus.TestInjection.inject(new CampusInventoryServiceImpl(), "campusInventoryMapper", mapper, "campusAccessService", f.access, "campusAssetStateService", f.assets, "campusMessageService", f.messages);
        expect(
                "完整覆盖",
                () ->
                        service.publish(
                                1L,
                                new InventoryPublishDTO(
                                        List.of(new InventoryPublishDTO.Assignment(1L, 100L)))));
        InventoryPublishDTO request =
                new InventoryPublishDTO(
                        List.of(
                                new InventoryPublishDTO.Assignment(2L, 100L),
                                new InventoryPublishDTO.Assignment(1L, 100L)));
        values.put("qualifiedUser", 0);
        expect("执行权限", () -> service.publish(1L, request));
        values.put("qualifiedUser", 1);
        f.events.clear();
        service.publish(1L, request);
        check(
                f.events.indexOf("asset:1") < f.events.indexOf("asset:2")
                        && f.events.indexOf("asset:2") < f.events.indexOf("selectTaskForChange"),
                "publication reads assets ascending before task");
        check(
                Collections.frequency(f.events, "insertItem") == 2,
                "publication freezes each asset exactly once");
        task.setPublishTime(LocalDateTime.now());
        expect("未发布", () -> service.publish(1L, request));
    }

    /**
     * 创建记录调用顺序并返回预设结果的 Mapper 测试代理。
     */
    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Map<String, Object> values, List<String> events) {
        return (T)
                Proxy.newProxyInstance(
                        type.getClassLoader(),
                        new Class<?>[] {type},
                        (p, method, args) -> {
                            events.add(method.getName());
                            if (method.getName().equals("insertAudit")) {
                                ((ScrapAuditVO) args[0]).setAuditId(99L);
                            }
                            if (values.containsKey(method.getName())) {
                                return values.get(method.getName());
                            }
                            if (method.getReturnType() == int.class) {
                                return 0;
                            }
                            if (method.getReturnType() == List.class) {
                                return List.of();
                            }
                            return null;
                        });
    }

    /**
     * 为测试线程设置指定用户的安全上下文。
     */
    private static void login(Long id) {
        SysUser user = new SysUser();
        user.setUserName("tester");
        LoginUser login = new LoginUser(id, 10L, user, Set.of());
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(login, null, List.of()));
    }

    /**
     * 执行操作并断言业务异常包含预期提示。
     */
    private static void expect(String text, Runnable action) {
        try {
            action.run();
            throw new AssertionError("Expected rejection: " + text);
        } catch (ServiceException ex) {
            check(
                    ex.getMessage().contains(text),
                    "rejection: " + text + " actual: " + ex.getMessage());
        }
    }

    /**
     * 检查断言条件，失败时抛出断言异常并在成功时累计计数。
     */
    private static void check(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label);
        }
        assertions++;
    }

    private static final class Fixture {

        final List<String> events = new ArrayList<>();

        final CampusAccessService access =
                new com.cfams.campus.service.impl.CampusAccessServiceImpl() {

                    /**
                     * 设置测试查询为不受数据范围限制。
                     */
                    @Override
                    public void scope(CampusQueryDTO query) {
                        query.setUnrestricted(true);
                    }

                    /**
                     * 测试替身忽略权限检查，聚焦业务状态边界。
                     */
                    @Override
                    public void requirePermission(String value) {
                    }

                    /**
                     * 测试替身忽略部门权限检查。
                     */
                    @Override
                    public void requireDepartment(Long id) {
                    }

                    /**
                     * 测试替身忽略用户有效性检查。
                     */
                    @Override
                    public void requireUser(Long id) {
                    }

                    /**
                     * 测试替身忽略数据访问范围检查。
                     */
                    @Override
                    public void requireData(Long dept, Long owner) {
                    }
                };

        final CampusAssetStateService assets =
                new com.cfams.asset.service.impl.CampusAssetStateServiceImpl() {

                    /**
                     * 记录资产读取顺序并返回固定状态的测试资产。
                     */
                    @Override
                    public AssetStateVO selectForChange(Long id) {
                        events.add("asset:" + id);
                        AssetStateVO state = new AssetStateVO();
                        state.setAssetId(id);
                        state.setDeptId(10L);
                        state.setLocationId(20L);
                        state.setKeeperUserId(30L);
                        state.setCategoryId(40L);
                        state.setAssetStatus("0");
                        state.setConditionStatus("0");
                        return state;
                    }

                    /**
                     * 返回用于校验台账一致性的固定测试快照。
                     */
                    @Override
                    public String snapshot(Long id) {
                        return SNAPSHOT;
                    }

                    /**
                     * 记录测试中的资产状态变更事件。
                     */
                    @Override
                    public void changeState(
                            Long id, String expected, String state, String condition) {
                        events.add("changeState");
                    }

                    /**
                     * 记录测试中的资产履历写入事件。
                     */
                    @Override
                    public void history(
                            Long id,
                            String event,
                            String type,
                            Long bizId,
                            String before,
                            String reason) {
                        events.add("history");
                    }
                };

        final CampusMessageService messages =
                new CampusMessageServiceImpl() {

                    /**
                     * 记录测试中的业务通知事件。
                     */
                    @Override
                    public void notify(
                            Long receiver,
                            String key,
                            String type,
                            String bizType,
                            Long id,
                            String title,
                            String content) {
                        events.add("notify");
                    }
                };
    }
}
