package com.cfams.asset.service;

import java.io.*;
import com.cfams.asset.service.impl.CampusAssetServiceImpl;
import com.cfams.asset.service.impl.CampusAssetExcelServiceImpl;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.*;
import jakarta.validation.Validation;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.apache.poi.ss.usermodel.*;
import com.cfams.common.exception.ServiceException;
import com.cfams.common.core.domain.entity.SysUser;
import com.cfams.common.core.domain.model.LoginUser;
import com.cfams.asset.domain.dto.*;
import com.cfams.asset.domain.vo.*;
import com.cfams.asset.mapper.CampusAssetMapper;
import com.cfams.campus.service.CampusAccessService;
import com.cfams.asset.service.CampusAssetStateService;
import com.cfams.asset.domain.vo.AssetStateVO;
import com.cfams.campus.domain.dto.CampusQueryDTO;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 无数据库依赖的回归检查，可直接运行main；不连接或写入真实库。
 * 模拟边界用于验证拒绝路径和写入次数，真实事务回滚仍需隔离库集成测试。
 */
public final class CampusAssetChecks {
    private static int checks;
    private static int inserts;
    private static int updates;
    private static int histories;
    private static String generatedToken;
    private static boolean validCategory = true;
    private static int references;
    private static String assetState = "0";
    private static final List<CategoryVO> categories = new ArrayList<>();

    /** 执行无需启动服务或连接数据库的业务回归检查。 */
    public static void main(String[] args) throws Exception {
        SysUser user = new SysUser();
        user.setUserId(1L);
        user.setUserName("checks");
        LoginUser login = new LoginUser(1L, 10L, user, Set.of("*:*:*"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(login, null, List.of()));
        try (jakarta.validation.ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            CampusAssetMapper mapper = (CampusAssetMapper) Proxy.newProxyInstance(
                    CampusAssetChecks.class.getClassLoader(), new Class<?>[] {CampusAssetMapper.class},
                    (proxy, method, values) -> {
                        switch (method.getName()) {
                        case "validUser": case "validDepartment": return 1L;
                        case "selectCategoryTree": return categories;
                        case "category": return categories.stream().filter(c -> c.getCategoryId().equals(values[0])).findFirst().orElse(null);
                        case "categoryReferences": case "locationReferences": return references;
                        case "selectCategoryForChange": return validCategory ? category(1L, null) : null;
                        case "selectLocationForChange": return location();
                        case "selectLocationTree": return List.of(location());
                        case "location": return location();
                        case "pendingBusiness": return 0;
                        case "assetIdByCode": return null;
                        case "insertAsset": {
                            inserts++;
                            generatedToken = (String) values[1];
                            ((AssetSaveDTO) values[0]).setAssetId((long) inserts);
                            return 1;
                        }
                        case "updateAsset": { updates++; return 1; }
                        case "insertCategory": case "updateCategory": return 1;
                        case "asset": return view();
                        default: throw new AssertionError("Unexpected mapper call: " + method.getName());
                        }
                    });
            CampusAccessService access = new com.cfams.campus.service.impl.CampusAccessServiceImpl() {
                /** 模拟部门数据范围并覆盖客户端伪造的权限条件。 */
                @Override public void scope(CampusQueryDTO q) {
                    q.setUnrestricted(false);
                    q.setAllowedDeptIds(List.of(10L));
                    q.setSelfUserId(null);
                }
                /** 模拟部门权限，拒绝非测试部门的资产入册。 */
                @Override public void requireDepartment(Long id) {
                    if (!Long.valueOf(10).equals(id)) throw new ServiceException("department denied");
                }
                /** 在测试中允许责任人校验通过，以隔离待验证的业务条件。 */
                @Override
                public void requireUser(Long id) {
                }
                /** 在测试中允许业务权限校验通过，以隔离台账校验条件。 */
                @Override
                public void requirePermission(String permission) {
                }
                /** 模拟资产数据权限，拒绝访问测试部门之外的数据。 */
                @Override public void requireData(Long dept, Long owner) {
                    if (!Long.valueOf(10).equals(dept)) throw new ServiceException("data denied");
                }
            };
            CampusAssetStateService coordinator = new com.cfams.asset.service.impl.CampusAssetStateServiceImpl() {
                /** 构造当前流程状态的已读取资产供台账编辑校验。 */
                @Override public AssetStateVO selectForChange(Long id) {
                    AssetStateVO state = new AssetStateVO();
                    state.setAssetCode("A-001");
                    state.setDeptId(10L);
                    state.setKeeperUserId(1L);
                    state.setAssetStatus(assetState);
                    return state;
                }
                /** 返回用于验证履历写入的固定资产快照。 */
                @Override
                public String snapshot(Long id) {
                    return "{}";
                }
                /** 统计资产履历写入次数，验证登记及编辑的审计行为。 */
                @Override public void history(Long id, String event, String type, Long biz, String before, String reason) {
                    histories++;
                }
            };
            CampusAssetService service = com.cfams.campus.TestInjection.inject(new CampusAssetServiceImpl(), "campusAssetMapper", mapper, "campusAccessService", access, "campusAssetStateService", coordinator, "validator", factory.getValidator());
            CampusAssetExcelService excel = com.cfams.campus.TestInjection.inject(new CampusAssetExcelServiceImpl(), "campusAssetService", service, "campusAccessService", access, "validator", factory.getValidator());
            checkTree();
            checkMapping();
            expectFailure(() -> { AssetSaveDTO a = input(); a.setDeptId(99L); service.create(a); });
            check(inserts == 0, "unauthorized department must not insert");
            for (String status : List.of("1", "2", "3")) {
                assetState = status;
                expectFailure(() -> { AssetSaveDTO a = input(); a.setAssetId(1L); service.update(a); });
            }
            check(updates == 0, "borrowed/repair/scrap must not update");
            assetState = "0";
            expectFailure(() -> { AssetSaveDTO a = input(); a.setAssetId(1L); a.setAssetCode("OTHER"); service.update(a); });
            validCategory = false;
            expectFailure(() -> service.create(input()));
            validCategory = true;
            CategoryVO root = category(1L, null);
            categories.add(root);
            references = 1;
            CategorySaveDTO disabled = new CategorySaveDTO();
            org.springframework.beans.BeanUtils.copyProperties(category(1L, null), disabled);
            disabled.setStatus("1");
            expectFailure(() -> service.saveCategory(disabled, false));
            LocationSaveDTO disabledLocation = new LocationSaveDTO();
            org.springframework.beans.BeanUtils.copyProperties(location(), disabledLocation);
            disabledLocation.setStatus("1");
            expectFailure(() -> service.saveLocation(disabledLocation, false));
            references = 0;
            CampusQueryDTO forged = new CampusQueryDTO();
            forged.setUnrestricted(true);
            forged.setAllowedDeptIds(List.of(999L));
            service.scope(forged);
            check(!forged.isUnrestricted() && forged.getAllowedDeptIds().equals(List.of(10L)), "scope override");
            ByteArrayOutputStream template = new ByteArrayOutputStream();
            excel.template(template);
            byte[] validFile = workbook(template.toByteArray(), false, false);
            AssetImportResultVO invalid = excel.importWorkbook(new ByteArrayInputStream(workbook(template.toByteArray(), true, false)));
            check(invalid.getImported() == 0 && !invalid.getErrors().isEmpty() && inserts == 0, "whole batch rejected");
            AssetImportResultVO formula = excel.importWorkbook(new ByteArrayInputStream(workbook(template.toByteArray(), false, true)));
            check(formula.getImported() == 0 && inserts == 0, "formula rejected without writes");
            AssetImportResultVO valid = excel.importWorkbook(new ByteArrayInputStream(validFile));
            check(valid.getImported() == 1 && inserts == 1 && histories == 1, "valid import and history");
            check(generatedToken.matches("[0-9a-f]{32}"), "stable token created once");
            AssetSaveDTO a = input();
            a.setAssetId(1L);
            service.update(a);
            check(updates == 1 && histories == 2, "edit history");
            List<AssetLabelVO> first = service.labels(List.of(1L));
            List<AssetLabelVO> second = service.labels(List.of(1L));
            check(first.equals(second), "reprint preserves token");
            ByteArrayOutputStream exported = new ByteArrayOutputStream();
            excel.export(List.of(view()), exported);
            try (Workbook book = WorkbookFactory.create(new ByteArrayInputStream(exported.toByteArray()))) {
                check(book.getSheetAt(0).getRow(1).getCell(0).getCellType() == CellType.STRING, "safe export string");
            }
            check(Arrays.stream(java.beans.Introspector.getBeanInfo(AssetSaveDTO.class).getPropertyDescriptors())
                    .noneMatch(p -> Set.of("assetStatus", "conditionStatus", "qrToken", "registeredTime", "scrappedTime")
                    .contains(p.getName())), "write model excludes flow fields");
            System.out.println("Campus asset checks passed: " + checks + "; no database connection used.");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    /** 调用实现类内部的树校验，验证自环、后代环和不存在父节点的拒绝行为。 */
    private static void invokeTreeCheck(Long id, Long parentId, Map<Long, Long> parents) {
        try {
            java.lang.reflect.Method method = CampusAssetServiceImpl.class.getDeclaredMethod(
                    "checkTree", Long.class, Long.class, Map.class);
            method.setAccessible(true);
            method.invoke(null, id, parentId, parents);
        } catch (java.lang.reflect.InvocationTargetException ex) {
            if (ex.getCause() instanceof RuntimeException) throw (RuntimeException) ex.getCause();
            throw new AssertionError(ex.getCause());
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    /** 验证树结构允许合法父节点并拒绝循环和缺失父节点。 */
    private static void checkTree() {
        Map<Long, Long> parents = new HashMap<>();
        parents.put(1L, null);
        parents.put(2L, 1L);
        invokeTreeCheck(3L, 2L, parents);
        checks++;
        expectFailure(() -> invokeTreeCheck(1L, 2L, parents));
        expectFailure(() -> invokeTreeCheck(3L, 88L, parents));
    }

    /** 解析资产Mapper并验证空权限、部门权限及全范围查询条件。 */
    private static void checkMapping() throws IOException {
        Configuration config = new Configuration();
        String resource = "mapper/pc/asset/CampusAssetMapper.xml";
        try (InputStream input = CampusAssetChecks.class.getClassLoader().getResourceAsStream(resource)) {
            if (input == null) throw new AssertionError("mapper resource missing");
            new XMLMapperBuilder(input, config, resource, config.getSqlFragments()).parse();
        }
        CampusQueryDTO query = new CampusQueryDTO();
        String statement = CampusAssetMapper.class.getName() + ".assets";
        String denied = config.getMappedStatement(statement).getBoundSql(query).getSql();
        check(denied.contains("1=0") && !denied.contains("1=1") && !denied.contains("IN ()"), "empty scope fail closed");
        query.setAllowedDeptIds(List.of(10L, 11L));
        query.setSelfUserId(5L);
        String scoped = config.getMappedStatement(statement).getBoundSql(query).getSql();
        check(scoped.contains("a.dept_id IN") && scoped.contains("a.keeper_user_id="), "department/owner scope");
        query.setUnrestricted(true);
        check(config.getMappedStatement(statement).getBoundSql(query).getSql().contains("1=1"), "unrestricted scope");
    }

    /** 构造正常、无效金额或公式单元格的资产导入测试文件。 */
    private static byte[] workbook(byte[] template, boolean badRow, boolean formula) throws IOException {
        try (Workbook book = WorkbookFactory.create(new ByteArrayInputStream(template))) {
            String[] fields = {"A-001", "Asset", "1", "", "2026-01-01", "100.00", "", "3", "10", "1", "1", "1", "1", ""};
            Row row = book.getSheetAt(0).createRow(1);
            for (int i = 0; i < fields.length; i++) row.createCell(i).setCellValue(fields[i]);
            if (formula) row.getCell(5).setCellFormula("1+1");
            if (badRow) {
                Row invalid = book.getSheetAt(0).createRow(2);
                for (int i = 0; i < fields.length; i++) invalid.createCell(i).setCellValue(fields[i]);
                invalid.getCell(0).setCellValue("A-002");
                invalid.getCell(5).setCellValue("-1");
            }
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            book.write(result);
            return result.toByteArray();
        }
    }

    /** 构造合法资产入册请求供拒绝路径和成功路径复用。 */
    private static AssetSaveDTO input() {
        AssetSaveDTO a = new AssetSaveDTO();
        a.setAssetCode("A-001");
        a.setAssetName("Asset");
        a.setCategoryId(1L);
        a.setPurchasePrice(new BigDecimal("100.00"));
        a.setDeptId(10L);
        a.setLocationId(1L);
        a.setKeeperUserId(1L);
        return a;
    }

    /** 构造具有稳定二维码标识的资产查询测试视图。 */
    private static AssetVO view() {
        AssetVO v = new AssetVO();
        v.setAssetId(1L);
        v.setAssetCode("A-001");
        v.setAssetName("Asset");
        v.setDeptId(10L);
        v.setKeeperUserId(1L);
        v.setQrToken(generatedToken);
        return v;
    }

    /** 构造指定主键及父节点的有效类别测试数据。 */
    private static CategoryVO category(Long id, Long parent) {
        CategoryVO c = new CategoryVO();
        c.setCategoryId(id);
        c.setParentId(parent);
        c.setCategoryCode("C-" + id);
        c.setCategoryName("Category");
        c.setStatus("0");
        c.setOrderNum(0);
        return c;
    }

    /** 构造测试部门下的有效场所数据。 */
    private static LocationVO location() {
        LocationVO l = new LocationVO();
        l.setLocationId(1L);
        l.setLocationCode("L-001");
        l.setLocationName("Location");
        l.setDeptId(10L);
        l.setLocationType("4");
        l.setStatus("0");
        l.setOrderNum(0);
        return l;
    }

    /** 执行应被业务规则拒绝的调用并核对业务异常。 */
    private static void expectFailure(Runnable call) {
        try { call.run(); } catch (ServiceException expected) { checks++; return; }
        throw new AssertionError("Expected business rejection");
    }

    /** 核对回归断言并累计通过数量。 */
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
}
