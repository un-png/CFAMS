package com.cfams.asset.service;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cfams.common.entity.*;
import com.cfams.asset.domain.dto.*;
import com.cfams.asset.domain.vo.*;
import com.cfams.borrow.domain.vo.BorrowVO;
import com.cfams.borrow.domain.vo.HandoverVO;
import com.cfams.repair.domain.vo.RepairVO;
import com.cfams.repair.domain.vo.RepairRecordVO;
import com.cfams.identity.domain.vo.*;
import com.cfams.inventory.domain.vo.InventoryTaskVO;
import com.cfams.inventory.domain.vo.InventoryItemVO;
import com.cfams.scrap.domain.vo.ScrapVO;
import com.cfams.scrap.domain.vo.ScrapAuditVO;
import com.cfams.asset.domain.vo.AssetStateVO;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;

/** 无数据库回归：实体复用、JSON字段兼容、敏感字段隔离与Mapper解析。 */
public final class CampusEntityChecks {
    private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();
    private static int checks;

    /** 执行无需启动服务或连接数据库的业务回归检查。 */
    public static void main(String[] args) throws Exception {
        fields(new AssetLabelVO(), "assetId assetCode assetName qrToken");
        fields(new AssetImportResultVO(), "total imported errors");
        fields(new AssetImportResultVO.RowErrorVO(), "row field reason");
        fields(new PersonImportResultVO(), "successCount failures");
        fields(new PersonImportResultVO.RowFailureVO(), "row reason");
        fields(new BindingCredentialVO(), "personId token expireTime");
        fields(new com.cfams.qrcode.domain.dto.AssetLabelBatchDTO(), "assetIds");
        com.cfams.qrcode.domain.dto.AssetLabelBatchDTO labels = JSON.readValue(
                "{\"assetIds\":[1,2]}", com.cfams.qrcode.domain.dto.AssetLabelBatchDTO.class);
        check(labels.getAssetIds().equals(List.of(1L, 2L)), "label request JSON binding changed");
        try (jakarta.validation.ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            check(factory.getValidator().validate(labels).isEmpty(), "valid label ids rejected");
            labels.setAssetIds(List.of(0L));
            check(!factory.getValidator().validate(labels).isEmpty(), "non-positive label id accepted");
            labels.setAssetIds(java.util.Collections.singletonList(null));
            check(!factory.getValidator().validate(labels).isEmpty(), "null label id accepted");
            labels.setAssetIds(java.util.Collections.emptyList());
            check(!factory.getValidator().validate(labels).isEmpty(), "empty label request accepted");
            labels.setAssetIds(java.util.Collections.nCopies(201, 1L));
            check(!factory.getValidator().validate(labels).isEmpty(), "oversize label request accepted");
        }
        fields(new AssetVO(), "assetId assetCode assetName categoryId specification purchaseDate purchasePrice supplierName expectedUseYears deptId locationId keeperUserId borrowEnabled visibility remark assetStatus conditionStatus qrToken registeredTime scrappedTime categoryName locationName deptName keeperName");
        fields(new CategoryVO(), "categoryId parentId categoryCode categoryName orderNum status remark");
        fields(new LocationVO(), "locationId parentId locationCode locationName locationType deptId orderNum status remark");
        fields(new AssetHistoryVO(), "historyId assetId eventType bizType bizId beforeSnapshot afterSnapshot operatorUserId eventTime changeReason");
        fields(new BorrowVO(), "borrowId borrowNo assetId assetCode assetName assetDeptId applicantUserId applicantName purpose plannedStartTime plannedEndTime status applyTime auditUserId auditTime auditOpinion voucherToken actualPickupTime actualReturnTime cancelUserId cancelTime cancelReason");
        fields(new HandoverVO(), "handoverId borrowId assetId assetDeptId applicantUserId requestUserId borrowNo assetName applicantName handoverType status requestTime requestNote confirmUserId confirmTime conditionStatus confirmNote");
        fields(new RepairVO(), "repairId repairNo assetId assetCode assetName assetDeptId borrowId reporterUserId reporterName faultType faultDescription faultTime faultLocation reportTime status acceptUserId acceptTime acceptOpinion repairerUserId repairerName assignTime startTime repairPlan repairCost repairResult resultDescription feedbackTime verifyUserId verifyTime verifyOpinion");
        fields(new RepairRecordVO(), "recordId repairId actionType fromStatus toStatus operatorUserId operatorName targetUserId content costSnapshot resultSnapshot actionTime");
        fields(new PersonVO(), "personId personNo personType realName deptId deptName userId userName status bindingState remark createTime");
        fields(new BindingVO(), "personId personNo personType realName deptId deptName userId userName status bindingState remark createTime bindingId appid bindingStatus bindTime lastLoginTime");
        fields(new InventoryTaskVO(), "taskId taskNo taskName deptId deptName scopeDeptId scopeLocationId scopeCategoryId startTime endTime status publisherUserId publishTime submitTime completeUserId completeTime createBy createTime updateTime remark totalCount checkedCount reviewedCount userIds");
        fields(new InventoryItemVO(), "itemId taskId assetId assetCode assetName assignedUserId assignedUserName baseSnapshot scanTime checkedUserId checkedTime observedLocationId observedCondition missingFlag checkNote reviewStatus reviewUserId reviewTime reviewNote reviewSnapshot resolvedLocationId resolvedCondition repairId scrapId createBy");
        fields(new ScrapVO(), "scrapId scrapNo assetId assetCode assetName assetDeptId deptName applicantUserId applicantName disposeType reason applyTime status completedTime createBy audits attachments");
        fields(new ScrapAuditVO(), "auditId scrapId auditLevel auditResult auditorUserId auditorName auditOpinion auditTime");
        fields(new AssetStateVO(), "assetId categoryId deptId locationId keeperUserId assetCode assetName assetStatus conditionStatus borrowEnabled visibility registeredTime scrappedTime purchasePrice");
        fields(new CampusAttachment(1L, "ASSET", 1L, "test.png", "test", "test.png", "image/png", 1L, 0, 1L, null),
                "attachmentId bizType bizId fileName bucketName objectKey contentType fileSize sortNum uploaderUserId uploadTime");
        fields(new CampusMessage(1L, "REPAIR", "REPAIR", 1L, "test", "test", "0", null, null, 1L, "internal"),
                "messageId messageType bizType bizId title content readFlag readTime createTime");

        CaPerson person = new CaPerson();
        person.setBindTokenHash("do-not-serialize");
        check(!JSON.writeValueAsString(person).contains("do-not-serialize"), "person hash leaked");
        PersonVO personView = new PersonVO();
        personView.setBindTokenHash("do-not-serialize");
        check(!JSON.writeValueAsString(personView).contains("bindTokenHash"), "inherited hash leaked");
        CaWxBinding binding = new CaWxBinding();
        binding.setOpenid("do-not-serialize");
        binding.setUnionid("do-not-serialize");
        check(!JSON.writeValueAsString(binding).contains("do-not-serialize"), "wechat identity leaked");
        for (Class<?> dto : List.of(AssetSaveDTO.class, CategorySaveDTO.class, LocationSaveDTO.class,
                com.cfams.identity.domain.dto.PersonSaveDTO.class)) {
            check(!dto.getSuperclass().getPackageName().equals("com.cfams.common.entity"), "request inherited entity");
            Set<String> writable = new HashSet<>();
            Arrays.stream(java.beans.Introspector.getBeanInfo(dto).getPropertyDescriptors())
                    .filter(p -> p.getWriteMethod() != null).forEach(p -> writable.add(p.getName()));
            check(java.util.Collections.disjoint(writable, Set.of("createBy", "createTime", "updateBy",
                    "updateTime", "assetStatus", "conditionStatus", "qrToken", "bindTokenHash", "openid")),
                    "request white list widened");
        }
        for (Class<?> model : List.of(AssetVO.class, CategoryVO.class, LocationVO.class, AssetHistoryVO.class,
                BorrowVO.class, HandoverVO.class, RepairVO.class, RepairRecordVO.class,
                PersonVO.class, InventoryTaskVO.class, InventoryItemVO.class, ScrapVO.class, ScrapAuditVO.class,
                AssetStateVO.class)) {
            check(model.getSuperclass().getPackageName().equals("com.cfams.common.entity"), "missing entity base");
            for (Field field : model.getDeclaredFields()) {
                check(Arrays.stream(model.getSuperclass().getFields()).noneMatch(f -> f.getName().equals(field.getName())),
                        "shadowed persistent field: " + field.getName());
            }
        }
        mapperCheck();
        System.out.println("Campus entity checks passed: " + checks + "; no database connection used.");
    }

    /** 解析已迁移业务Mapper并核对实体参数及返回类型。 */
    private static void mapperCheck() throws Exception {
        Configuration configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        for (String group : List.of("asset/CampusAssetMapper", "borrow/CampusBorrowMapper",
                "repair/CampusRepairMapper", "identity/CampusIdentityMapper",
                "inventory/CampusInventoryMapper", "scrap/CampusScrapMapper",
                "attachment/CampusAttachmentMapper", "message/CampusMessageMapper", "campus/CampusAccessMapper")) {
            String resource = "mapper/pc/" + group + ".xml";
            try (InputStream input = CampusEntityChecks.class.getClassLoader().getResourceAsStream(resource)) {
                if (input == null) throw new AssertionError("Missing " + resource);
                new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
            }
            checks++;
        }
        check(configuration.getMappedStatement("com.cfams.identity.mapper.CampusIdentityMapper.removeBinding")
                .getParameterMap().getType() == CaWxBinding.class, "binding mapper must use entity");
        check(configuration.getMappedStatement("com.cfams.inventory.mapper.CampusInventoryMapper.insertUser")
                .getParameterMap().getType() == CaInventoryUser.class, "member mapper must use entity");
        check(configuration.getMappedStatement("com.cfams.message.mapper.CampusMessageMapper.detail")
                .getResultMaps().get(0).getType() == CampusMessage.class, "message mapper must use relocated entity");
        check(configuration.getMappedStatement("com.cfams.attachment.mapper.CampusAttachmentMapper.detail")
                .getResultMaps().get(0).getType() == CampusAttachment.class, "attachment mapper must use relocated entity");
    }

    /** 比较JSON字段集合以检测响应字段遗漏或泄露。 */
    private static void fields(Object value, String expected) {
        Set<String> actual = new HashSet<>();
        JSON.valueToTree(value).fieldNames().forEachRemaining(actual::add);
        Set<String> wanted = new HashSet<>(List.of(expected.split(" ")));
        check(actual.equals(wanted), value.getClass().getSimpleName() + " JSON changed: " + actual);
    }

    /** 核对回归断言并累计通过数量。 */
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
}
