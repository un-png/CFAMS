package com.cfams.campus;

import com.cfams.campus.domain.vo.CampusOptionVO;
import com.cfams.common.entity.CampusAttachment;
import com.cfams.common.entity.CampusMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

/** 无数据库检查：业务Mapper迁移、JavaBean字段映射及JSON兼容。 */
public class BackendStructureChecks {
    private static int checks;

    /** 解析PC模块全部XML，验证命名空间和共享实体的序列化约定。 */
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        check(!Files.exists(Paths.get("cfams-pc/src/main/java/com/cfams/campus/core")),
                "Obsolete mixed core package remains");
        check(!Files.exists(Paths.get("cfams-pc/src/main/resources/mapper/pc/core")),
                "Obsolete core mapper directory remains");
        configuration.setMapUnderscoreToCamelCase(true);
        Path root = Paths.get("cfams-pc/src/main/resources/mapper/pc");
        List<Path> files;
        try (java.util.stream.Stream<Path> paths = Files.walk(root)) {
            files = paths.filter(path -> path.toString().endsWith("Mapper.xml"))
                    .collect(Collectors.toList());
        }
        check(files.size() >= 11, "Missing migrated mapper resources");
        for (Path file : files) {
            String xml = new String(Files.readAllBytes(file), java.nio.charset.StandardCharsets.UTF_8);
            check(!java.util.regex.Pattern.compile("FOR\\s+UPDATE", java.util.regex.Pattern.CASE_INSENSITIVE)
                    .matcher(xml).find(), "Pessimistic select remains in XML: " + file);
            try (InputStream input = Files.newInputStream(file)) {
                new XMLMapperBuilder(input, configuration, file.toString(),
                        configuration.getSqlFragments()).parse();
            }
        }
        int annotationCount = 0;
        for (Class<?> mapper : configuration.getMapperRegistry().getMappers()) {
            if (!mapper.getName().startsWith("com.cfams.")) {
                continue;
            }
            for (java.lang.reflect.Method method : mapper.getMethods()) {
                String[] sql = annotationSql(method);
                if (sql == null) {
                    continue;
                }
                annotationCount++;
                check(!java.util.regex.Pattern.compile("FOR\\s+UPDATE", java.util.regex.Pattern.CASE_INSENSITIVE)
                        .matcher(String.join(" ", sql)).find(), "Pessimistic select remains in annotation");
                String id = mapper.getName() + "." + method.getName();
                check(configuration.hasStatement(id), "Missing annotation SQL: " + id);
            }
        }
        check(annotationCount == 77, "Unexpected simple SQL annotation count: " + annotationCount);
        for (String name : new ArrayList<>(configuration.getMappedStatementNames())) {
            if (!name.contains(".")) {
                continue;
            }
            check(!name.startsWith("com.cfams.system.campus."), "Old mapper namespace remains");
            check(!name.startsWith("com.cfams.campus.core."), "Mixed core mapper namespace remains");
            String namespace = name.substring(0, name.lastIndexOf('.'));
            Class<?> mapper = Class.forName(namespace);
            String method = name.substring(name.lastIndexOf('.') + 1);
            boolean declared = false;
            for (java.lang.reflect.Method candidate : mapper.getMethods()) {
                if (candidate.getName().equals(method)) {
                    declared = true;
                    break;
                }
            }
            check(declared, "XML statement has no mapper method: " + name);
        }
        ObjectMapper json = new ObjectMapper().findAndRegisterModules();
        CampusOptionVO option = new CampusOptionVO(7L, "教学设备");
        JsonNode optionJson = json.readTree(json.writeValueAsString(option));
        check(optionJson.size() == 2 && optionJson.get("value").asLong() == 7L
                && "教学设备".equals(optionJson.get("label").asText()), "Option JSON changed");
        CampusAttachment attachment = json.readValue(
                "{\"attachmentId\":8,\"bizType\":\"ASSET\",\"bizId\":9,\"fileName\":\"photo.png\"}",
                CampusAttachment.class);
        check(attachment.getAttachmentId() == 8L && attachment.getBizId() == 9L,
                "Attachment bean cannot deserialize");
        CampusMessage message = new CampusMessage(1L, "REPAIR", "REPAIR", 2L,
                "测试通知", "测试内容", "0", null, null, 99L, "private-event");
        JsonNode messageJson = json.readTree(json.writeValueAsString(message));
        check(messageJson.has("messageId") && !messageJson.has("receiverUserId")
                && !messageJson.has("eventKey"), "Private message fields leaked");
        check(!CampusOptionVO.class.isRecord() && !CampusAttachment.class.isRecord()
                && !CampusMessage.class.isRecord(), "Record model remains");
        checkReminderInitialization();
        System.out.println("Backend structure checks passed: " + checks
                + "; mapper XML files: " + files.size() + "; no database connection used.");
    }

    /** 检查字段注入后的初始化仍使用独立提醒事务和读已提交隔离级别。 */
    private static void checkReminderInitialization() throws Exception {
        org.springframework.transaction.PlatformTransactionManager manager =
                (org.springframework.transaction.PlatformTransactionManager) java.lang.reflect.Proxy.newProxyInstance(
                        BackendStructureChecks.class.getClassLoader(),
                        new Class<?>[] {org.springframework.transaction.PlatformTransactionManager.class},
                        (proxy, method, arguments) -> {
                            throw new AssertionError("此检查不应开启真实事务");
                        });
        com.cfams.borrow.service.impl.CampusBorrowServiceImpl service =
                TestInjection.inject(new com.cfams.borrow.service.impl.CampusBorrowServiceImpl(),
                        "platformTransactionManager", manager);
        service.initializeReminderTransaction();
        java.lang.reflect.Field field = service.getClass().getDeclaredField("reminderTransaction");
        field.setAccessible(true);
        org.springframework.transaction.support.TransactionTemplate template =
                (org.springframework.transaction.support.TransactionTemplate) field.get(service);
        check(template.getTransactionManager() == manager, "Reminder transaction manager changed");
        check(template.getIsolationLevel() == org.springframework.transaction.TransactionDefinition.ISOLATION_READ_COMMITTED,
                "Reminder isolation changed");
        check(template.getPropagationBehavior() == org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW,
                "Reminder propagation changed");
        check(service.getClass().getMethod("initializeReminderTransaction")
                .isAnnotationPresent(jakarta.annotation.PostConstruct.class), "Missing lifecycle initialization");
    }

    /** 读取固定CRUD注解的SQL片段；复杂XML方法返回空。 */
    private static String[] annotationSql(java.lang.reflect.Method method) {
        Select select = method.getAnnotation(Select.class);
        if (select != null) return select.value();
        Insert insert = method.getAnnotation(Insert.class);
        if (insert != null) return insert.value();
        Update update = method.getAnnotation(Update.class);
        if (update != null) return update.value();
        Delete delete = method.getAnnotation(Delete.class);
        if (delete != null) return delete.value();
        return null;
    }

    /** 记录检查结果，失败时立即中止并说明映射问题。 */
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
