package com.cfams.identity.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.cfams.identity.service.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.io.InputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.*;
import jakarta.validation.Validator;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import com.cfams.common.exception.ServiceException;
import com.cfams.common.utils.SecurityUtils;
import com.cfams.common.core.domain.entity.SysUser;
import com.cfams.campus.service.CampusAccessService;
import com.cfams.identity.domain.dto.*;
import com.cfams.identity.domain.vo.*;
import com.cfams.identity.mapper.CampusIdentityMapper;
import com.cfams.system.service.ISysConfigService;

/** 人员维护和管理员绑定核验，不实现微信登录。 */
@Service
public class CampusIdentityServiceImpl implements CampusIdentityService {
    @Autowired
    private CampusIdentityMapper campusIdentityMapper;
    @Autowired
    private CampusAccessService campusAccessService;
    @Autowired
    private Validator validator;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private ISysConfigService iSysConfigService;
    private final SecureRandom random = new SecureRandom();

    /** 计算数据权限后查询人员名册。 */
    @Override
    public List<PersonVO> people(PersonQueryDTO query) {
        campusAccessService.scope(query);
        return campusIdentityMapper.people(query);
    }

    /** 计算数据权限后查询人员与微信绑定管理列表。 */
    @Override
    public List<BindingVO> bindings(BindingQueryDTO query) {
        campusAccessService.scope(query);
        return campusIdentityMapper.bindings(query);
    }

    /** 读取人员详情并验证当前用户的数据访问范围。 */
    @Override
    public PersonVO detail(Long id) {
        return requirePerson(id);
    }

    /** 读取人员记录，同时校验部门和关联用户权限。 */
    private PersonVO requirePerson(Long id) {
        PersonVO person = campusIdentityMapper.person(id);
        if (person == null) throw new ServiceException("人员不存在");
        campusAccessService.requireData(person.getDeptId(), person.getUserId());
        return person;
    }

    /** 在事务中保存人员名册及关联账号部门。 */
    @Transactional
    @Override
    public void save(Long id, PersonSaveDTO dto) {
        saveRow(id, dto);
    }

    /** 先校验人员记录再保存人员及账号关联数据。 */
    private void saveRow(Long id, PersonSaveDTO dto) {
        validateRow(id, dto);
        writeRow(id, dto);
    }

    /** 校验人员字段、账号替换限制、部门权限及学工号唯一性。 */
    private void validateRow(Long id, PersonSaveDTO dto) {
        Set<jakarta.validation.ConstraintViolation<PersonSaveDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ServiceException(violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage()).sorted()
                    .collect(java.util.stream.Collectors.joining("; ")));
        }
        PersonVO old = id == null ? null : requirePerson(id);
        // 已关联账号不在普通编辑中替换，防止遗留有效微信绑定被转移到另一个人。
        if (old != null && old.getUserId() != null && !old.getUserId().equals(dto.getUserId())) {
            throw new ServiceException("已关联账号不可在名册编辑中替换或解除");
        }
        campusAccessService.requireDepartment(dto.getDeptId());
        if (campusIdentityMapper.activeDepartment(dto.getDeptId()) != 1) throw new ServiceException("部门不存在或已停用");
        if (dto.getUserId() != null) {
            campusAccessService.requireUser(dto.getUserId());
            SysUser user = campusIdentityMapper.selectActiveUser(dto.getUserId());
            if (user == null) throw new ServiceException("关联账号不存在、已删除或已停用");
            campusAccessService.requireData(user.getDeptId(), user.getUserId());
        }
        if (campusIdentityMapper.duplicate(id, dto) != 0) throw new ServiceException("学工号或关联账号已被其他人员使用");
    }

    /** 写入人员资料并同步账号部门，停用时清除绑定凭据。 */
    private void writeRow(Long id, PersonSaveDTO dto) {
        String actor = SecurityUtils.getUsername();
        try {
            if (id == null) campusIdentityMapper.insert(dto, actor);
            else campusIdentityMapper.update(id, dto, actor);
            if (dto.getUserId() != null
                    && campusIdentityMapper.syncDepartment(dto.getUserId(), dto.getDeptId(), actor) != 1) {
                throw new ServiceException("关联账号状态已改变，请刷新后重试");
            }
            if (id != null && "1".equals(dto.getStatus())) campusIdentityMapper.invalidateToken(id, actor);
        } catch (DuplicateKeyException ex) {
            throw new ServiceException("学工号或关联账号已存在");
        }
    }

    /** 读取人员记录后停用名册并使绑定凭据失效。 */
    @Transactional
    @Override
    public void disable(Long id) {
        requirePerson(id);
        campusIdentityMapper.disable(id, SecurityUtils.getUsername());
    }

    /** 解析人员Excel并在同一事务中整批校验和保存，失败时保留实际行号。 */
    @Override
    public PersonImportResultVO importFile(InputStream input) throws IOException {
        try (Workbook book = WorkbookFactory.create(input)) {
            if (book.getNumberOfSheets() == 0) throw new ServiceException("Excel没有工作表");
            Sheet sheet = book.getSheetAt(0);
            if (sheet.getLastRowNum() < 1 || sheet.getLastRowNum() > 5000)
                throw new ServiceException("导入数据区必须为1至5000行");
            DataFormatter formatter = new DataFormatter(Locale.ROOT);
            Map<String, Integer> columns = new HashMap<>();
            Row header = sheet.getRow(0);
            if (header == null) throw new ServiceException("缺少模板表头");
            for (Cell cell : header) columns.put(formatter.formatCellValue(cell).trim(), cell.getColumnIndex());
            if (!columns.keySet().containsAll(Arrays.asList("学号或工号", "人员类型", "姓名", "部门ID")))
                throw new ServiceException("表头不完整，请使用人员导入模板");
            PersonImportResultVO result = new PersonImportResultVO();
            List<ImportRowDTO> rows = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row excelRow = sheet.getRow(i);
                if (excelRow == null) continue;
                boolean empty = true;
                for (Cell cell : excelRow) if (!formatter.formatCellValue(cell).isBlank()) empty = false;
                if (empty) continue;
                try {
                    PersonSaveDTO row = readRow(excelRow, columns, formatter);
                    rows.add(new ImportRowDTO(i + 1, row));
                } catch (ServiceException ex) {
                    result.getFailures().add(new PersonImportResultVO.RowFailureVO(i + 1, ex.getMessage()));
                } catch (NumberFormatException ex) {
                    result.getFailures().add(new PersonImportResultVO.RowFailureVO(i + 1, "部门ID和用户ID必须为有效整数"));
                }
            }
            if (rows.isEmpty() && result.getFailures().isEmpty()) throw new ServiceException("没有可导入的人员数据");
            // 先校验整批，再写入；业务错误和并发唯一键冲突都使同一事务整体回滚。
            transactionTemplate.executeWithoutResult(transaction -> {
                Map<String, Integer> numbers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                Map<Long, Integer> users = new HashMap<>();
                for (ImportRowDTO row : rows) {
                    try {
                        validateRow(null, row.getValue());
                        Integer previous = numbers.putIfAbsent(row.getValue().getPersonNo(), row.getNumber());
                        if (previous != null) throw new ServiceException("学工号与第" + previous + "行重复");
                        if (row.getValue().getUserId() != null) {
                            previous = users.putIfAbsent(row.getValue().getUserId(), row.getNumber());
                            if (previous != null) throw new ServiceException("关联用户与第" + previous + "行重复");
                        }
                    } catch (ServiceException ex) {
                        result.getFailures().add(new PersonImportResultVO.RowFailureVO(row.getNumber(), ex.getMessage()));
                    }
                }
                if (!result.getFailures().isEmpty()) {
                    transaction.setRollbackOnly();
                    return;
                }
                for (ImportRowDTO row : rows) {
                    try {
                        writeRow(null, row.getValue());
                    } catch (ServiceException | org.springframework.dao.DataAccessException ex) {
                        transaction.setRollbackOnly();
                        result.getFailures().add(new PersonImportResultVO.RowFailureVO(row.getNumber(),
                                "批次保存冲突，整批未导入，请刷新后重试"));
                        return;
                    }
                }
            });
            result.getFailures().sort(Comparator.comparingInt(PersonImportResultVO.RowFailureVO::getRow));
            if (result.getFailures().isEmpty()) result.setSuccessCount(rows.size());
            return result;
        }
    }

    /** 待导入人员行 */
    @lombok.Getter
    @lombok.Setter
    private static class ImportRowDTO {
        private int number;
        private PersonSaveDTO value;

        /** 创建空的待导入人员行，供请求绑定或逐项赋值使用。 */
        public ImportRowDTO() {
        }

        /** 使用完整字段创建待导入人员行。 */
        public ImportRowDTO(int number, PersonSaveDTO value) {
            this.number = number;
            this.value = value;
        }

    }

    /** 配置缺失、非法或超界时使用30分钟；允许1至1440分钟。 */
    private int tokenExpireMinutes() {
        String value = iSysConfigService.selectConfigByKey("campus.bind.expireMinutes");
        try {
            int minutes = Integer.parseInt(value == null ? "" : value.trim());
            return minutes >= 1 && minutes <= 1440 ? minutes : 30;
        } catch (NumberFormatException ex) {
            return 30;
        }
    }

    /** 按表头映射读取人员字段并转换人员类型及启停状态。 */
    private PersonSaveDTO readRow(Row row, Map<String, Integer> columns, DataFormatter formatter) {
        PersonSaveDTO dto = new PersonSaveDTO();
        dto.setPersonNo(cell(row, columns, formatter, "学号或工号"));
        dto.setRealName(cell(row, columns, formatter, "姓名"));
        String type = cell(row, columns, formatter, "人员类型");
        dto.setPersonType("学生".equals(type) ? "1" : "教职工".equals(type) ? "2" : type);
        String department = cell(row, columns, formatter, "部门ID");
        dto.setDeptId(department.isEmpty() ? null : Long.valueOf(department));
        String user = cell(row, columns, formatter, "关联用户ID");
        dto.setUserId(user.isEmpty() ? null : Long.valueOf(user));
        String status = cell(row, columns, formatter, "状态");
        dto.setStatus(status.isEmpty() || "有效".equals(status) ? "0" : "停用".equals(status) ? "1" : status);
        dto.setRemark(cell(row, columns, formatter, "备注"));
        return dto;
    }

    /** 按列名读取人员导入单元格，拒绝公式或错误值。 */
    private String cell(Row row, Map<String, Integer> columns, DataFormatter formatter, String name) {
        Integer index = columns.get(name);
        if (index == null) return "";
        Cell cell = row.getCell(index);
        if (cell != null && (cell.getCellType() == CellType.FORMULA || cell.getCellType() == CellType.ERROR))
            throw new ServiceException(name + "不允许公式或错误单元格");
        return formatter.formatCellValue(cell).trim();
    }

    /** 核验人员及账号状态，生成一次性明文凭据并仅保存摘要和过期时间。 */
    @Transactional
    @Override
    public BindingCredentialVO issue(Long id) {
        PersonVO person = requirePerson(id);
        if (!"0".equals(person.getStatus())) throw new ServiceException("停用人员不能发放凭据");
        if (campusIdentityMapper.activeDepartment(person.getDeptId()) != 1) throw new ServiceException("人员部门已停用");
        if (person.getUserId() != null && campusIdentityMapper.selectActiveUser(person.getUserId()) == null)
            throw new ServiceException("关联账号不可用");
        if ("BOUND".equals(person.getBindingState())) throw new ServiceException("已有有效绑定，请先完成换绑核验");
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Date expires = new Date(System.currentTimeMillis() + tokenExpireMinutes() * 60 * 1000L);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte part : digest) {
                hex.append(Character.forDigit((part & 0xff) >>> 4, 16));
                hex.append(Character.forDigit(part & 0x0f, 16));
            }
            String hash = hex.toString();
            campusIdentityMapper.issue(id, hash, expires, SecurityUtils.getUsername());
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
        return new BindingCredentialVO(id, token, expires);
    }

    /** 核验学工号及绑定归属后解除指定旧绑定并撤销旧凭据。 */
    @Transactional
    @Override
    public void rebind(Long id, BindingRebindDTO dto) {
        PersonVO person = requirePerson(id);
        if (!person.getPersonNo().equals(dto.getPersonNo()) || person.getUserId() == null)
            throw new ServiceException("人员核验不通过");
        com.cfams.common.entity.CaWxBinding binding = new com.cfams.common.entity.CaWxBinding();
        binding.setBindingId(dto.getBindingId());
        binding.setUserId(person.getUserId());
        if (campusIdentityMapper.removeBinding(binding) != 1)
            throw new ServiceException("指定绑定不存在或不属于该人员");
        campusIdentityMapper.invalidateToken(id, SecurityUtils.getUsername());
    }
}
