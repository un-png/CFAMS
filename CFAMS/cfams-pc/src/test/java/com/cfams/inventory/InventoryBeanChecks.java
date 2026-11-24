package com.cfams.inventory;

import com.cfams.inventory.domain.dto.*;
import com.cfams.inventory.domain.vo.*;
import com.cfams.scrap.domain.dto.*;
import com.cfams.scrap.domain.vo.*;
import com.cfams.statistics.domain.dto.StatisticsQueryDTO;
import com.cfams.statistics.domain.vo.*;
import com.cfams.statistics.mapper.CampusStatisticsMapper;
import com.cfams.statistics.service.CampusStatisticsService;
import com.cfams.statistics.service.impl.CampusStatisticsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 验证普通 JavaBean 保持 JSON 字段、请求校验及统计 Mapper 绑定兼容。
 */
final class InventoryBeanChecks {

    private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

    /**
     * 禁止实例化仅包含回归检查的工具类。
     */
    private InventoryBeanChecks() {
    }

    /**
     * 检查字段契约、嵌套请求校验与重命名后的统计映射。
     */
    static void verify() throws Exception {
        fields(
                new InventoryTaskSaveDTO(),
                "taskName deptId scopeDeptId scopeLocationId scopeCategoryId startTime endTime"
                        + " userIds remark");
        fields(new InventoryPublishDTO(), "assignments");
        fields(new InventoryPublishDTO.Assignment(), "assetId userId");
        fields(
                new InventoryReviewDTO(),
                "approved reviewNote recordOnly resolvedLocationId resolvedCondition repairId"
                        + " createRepair faultType faultDescription faultLocation");
        fields(new ScrapCreateDTO(), "assetId disposeType reason");
        fields(new ScrapAuditDTO(), "approved opinion");
        fields(
                new InventoryTaskVO(),
                "taskId taskNo taskName deptId deptName scopeDeptId scopeLocationId scopeCategoryId"
                        + " startTime endTime status publisherUserId publishTime submitTime"
                        + " completeUserId completeTime createBy createTime updateTime remark"
                        + " totalCount checkedCount reviewedCount userIds");
        fields(
                new InventoryItemVO(),
                "itemId taskId assetId assetCode assetName assignedUserId assignedUserName"
                    + " baseSnapshot scanTime checkedUserId checkedTime observedLocationId"
                    + " observedCondition missingFlag checkNote reviewStatus reviewUserId"
                    + " reviewTime reviewNote reviewSnapshot resolvedLocationId resolvedCondition"
                    + " repairId scrapId createBy");
        fields(
                new ScrapVO(),
                "scrapId scrapNo assetId assetCode assetName assetDeptId deptName applicantUserId"
                    + " applicantName disposeType reason applyTime status completedTime createBy"
                    + " audits attachments");
        fields(
                new ScrapAuditVO(),
                "auditId scrapId auditLevel auditResult auditorUserId auditorName auditOpinion"
                        + " auditTime");
        fields(
                new DashboardVO(),
                "currentGrouping groupingBasis stock pending rates categories departments trend"
                        + " limitations");
        fields(
                new RateVO(),
                "periodAssetCount borrowedAssetCount repairedAssetCount borrowRate repairRate"
                        + " unit");

        InventoryPublishDTO request =
                JSON.readValue(
                        "{\"assignments\":[{\"assetId\":1,\"userId\":2}]}",
                        InventoryPublishDTO.class);
        check(request.getAssignments().get(0).getUserId().equals(2L), "nested request binding");
        ScrapAuditDTO audit =
                JSON.readValue("{\"approved\":false,\"opinion\":\"退回\"}", ScrapAuditDTO.class);
        check(Boolean.FALSE.equals(audit.getApproved()), "Boolean false must survive binding");
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            check(validator.validate(request).isEmpty(), "valid assignment rejected");
            request.getAssignments().get(0).setAssetId(-1L);
            check(!validator.validate(request).isEmpty(), "nested positive constraint lost");
            check(
                    !validator.validate(new InventoryTaskSaveDTO()).isEmpty(),
                    "draft required constraints lost");
            check(
                    !validator.validate(new ScrapAuditDTO(true, "")).isEmpty(),
                    "audit opinion constraint lost");
        }
        check(
                CampusStatisticsService.class.isInterface(),
                "statistics service must be an interface");
        check(
                CampusStatisticsService.class.isAssignableFrom(CampusStatisticsServiceImpl.class),
                "statistics implementation must implement its service interface");
        queryContracts();
        statisticsMapper();
        System.out.println(
                "InventoryBeanChecks passed: JSON fields, binding, validation and statistics XML");
    }

    /**
     * 验证任务与明细查询职责分离，并保留报废筛选参数的绑定能力。
     */
    private static void queryContracts() throws Exception {
        InventoryTaskQueryDTO task = new InventoryTaskQueryDTO();
        InventoryItemQueryDTO item = new InventoryItemQueryDTO();
        check(
                InventoryTaskQueryDTO.class.isAssignableFrom(item.getClass()),
                "item query must reuse task filters");
        Set<String> taskProperties = new HashSet<>();
        for (java.beans.PropertyDescriptor property :
                java.beans.Introspector.getBeanInfo(task.getClass()).getPropertyDescriptors()) {
            taskProperties.add(property.getName());
        }
        check(
                !taskProperties.contains("taskId")
                        && !taskProperties.contains("reviewStatus")
                        && !taskProperties.contains("difference"),
                "item filters leaked into task query");
        ScrapQueryDTO scrap =
                JSON.readValue(
                        "{\"disposeType\":\"1\",\"handled\":true,\"applicant\":\"申请人\",\"deptId\":10}",
                        ScrapQueryDTO.class);
        check(
                "1".equals(scrap.getDisposeType())
                        && Boolean.TRUE.equals(scrap.getHandled())
                        && "申请人".equals(scrap.getApplicant())
                        && Long.valueOf(10L).equals(scrap.getDeptId()),
                "scrap query binding changed");
    }

    /**
     * 比较序列化字段集合，防止继承实体或 getter 改造扩大响应字段。
     */
    private static void fields(Object bean, String expected) {
        Set<String> actual = new HashSet<>();
        JSON.valueToTree(bean).fieldNames().forEachRemaining(actual::add);
        check(
                actual.equals(new HashSet<>(Arrays.asList(expected.split(" ")))),
                bean.getClass().getSimpleName() + " JSON fields changed: " + actual);
    }

    /**
     * 解析统计模块 XML 并验证三种分组维度和所有查询语句均可绑定。
     */
    private static void statisticsMapper() throws Exception {
        String resource = "mapper/pc/statistics/CampusStatisticsMapper.xml";
        Configuration configuration = new Configuration();
        try (InputStream input =
                InventoryBeanChecks.class.getClassLoader().getResourceAsStream(resource)) {
            check(input != null, "statistics XML missing");
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments())
                    .parse();
        }
        StatisticsQueryDTO query = new StatisticsQueryDTO();
        for (String dimension : Arrays.asList("MONTH", "CATEGORY", "DEPARTMENT")) {
            query.setDimension(dimension);
            for (String method :
                    Arrays.asList(
                            "stock", "pending", "rates", "categories", "departments", "reports")) {
                String sql =
                        configuration
                                .getMappedStatement(
                                        CampusStatisticsMapper.class.getName() + "." + method)
                                .getBoundSql(query)
                                .getSql();
                check(!sql.isBlank(), "empty statistics SQL: " + method);
            }
        }
    }

    /**
     * 断言检查条件成立，失败时抛出带具体说明的错误。
     */
    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
