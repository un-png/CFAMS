package com.cfams.asset.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.cfams.asset.service.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import jakarta.validation.Validator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cfams.common.exception.ServiceException;
import com.cfams.asset.domain.dto.*;
import com.cfams.asset.domain.vo.*;
import com.cfams.asset.domain.vo.AssetImportResultVO.RowErrorVO;
import com.cfams.campus.service.CampusAccessService;

/**
 * 统一模板、逐单元格错误报告、整批校验后事务入册。
 * 导入仅接受值单元格，不计算客户端公式。
 */
@Service
public class CampusAssetExcelServiceImpl implements CampusAssetExcelService {
    public static final int MAX_ROWS = 2000;
    private static final String[] HEADERS = {
        "资产编号", "资产名称", "类别ID", "规格型号", "采购日期", "采购金额", "供应商",
        "预计使用年限", "部门ID", "场所ID", "责任人ID", "开放借用(0/1)", "可见范围(0/1)", "备注"
    };
    private static final String[] FIELDS = {
        "assetCode", "assetName", "categoryId", "specification", "purchaseDate", "purchasePrice",
        "supplierName", "expectedUseYears", "deptId", "locationId", "keeperUserId", "borrowEnabled", "visibility", "remark"
    };
    @Autowired
    private CampusAssetService campusAssetService;
    @Autowired
    private CampusAccessService campusAccessService;
    @Autowired
    private Validator validator;

    /** 有任何一行不合法即返回 errors，所有行均不写入。 */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public AssetImportResultVO importWorkbook(InputStream input) throws IOException {
        campusAccessService.requirePermission("campus:assets:import");
        List<RowErrorVO> errors = new ArrayList<>();
        List<AssetSaveDTO> values = new ArrayList<>();
        List<Integer> rowNumbers = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(input)) {
            if (workbook.getNumberOfSheets() != 1) throw new ServiceException("导入文件只能包含一个工作表");
            Sheet sheet = workbook.getSheetAt(0);
            validateHeader(sheet.getRow(0));
            if (sheet.getLastRowNum() > MAX_ROWS) throw new ServiceException("每批最多导入2000行");
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (isBlank(row)) continue;
                values.add(parse(row, rowIndex + 1, errors));
                rowNumbers.add(rowIndex + 1);
            }
        }
        if (values.isEmpty()) throw new ServiceException("导入文件没有资产数据");
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < values.size(); i++) {
            AssetSaveDTO value = values.get(i);
            int row = rowNumbers.get(i);
            validator.validate(value).forEach(v ->
                    errors.add(new RowErrorVO(row, v.getPropertyPath().toString(), v.getMessage())));
            if (value.getAssetCode() != null && !codes.add(value.getAssetCode().strip().toLowerCase(Locale.ROOT))) {
                errors.add(new RowErrorVO(row, "assetCode", "资产编号在本批次重复"));
            }
            if (errors.stream().noneMatch(e -> e.getRow() == row)) {
                try {
                    campusAssetService.validateNewAsset(value);
                } catch (ServiceException e) {
                    errors.add(new RowErrorVO(row, "record", e.getMessage()));
                }
            }
        }
        if (!errors.isEmpty()) return new AssetImportResultVO(values.size(), 0, java.util.Collections.unmodifiableList(new ArrayList<>(errors)));
        for (int i = 0; i < values.size(); i++) {
            try {
                campusAssetService.insertValidated(values.get(i));
            } catch (ServiceException e) {
                throw new ServiceException("第" + rowNumbers.get(i) + "行：" + e.getMessage());
            }
        }
        return new AssetImportResultVO(values.size(), values.size(), java.util.Collections.emptyList());
    }

    /** 核对资产导入模板的列数与每列标题。 */
    private static void validateHeader(Row row) {
        if (row == null || row.getLastCellNum() != HEADERS.length) {
            throw new ServiceException("模板列不匹配，请下载资产导入模板");
        }
        for (int i = 0; i < HEADERS.length; i++) {
            if (!HEADERS[i].equals(text(row.getCell(i)))) {
                throw new ServiceException("模板第" + (i + 1) + "列应为：" + HEADERS[i]);
            }
        }
    }

    /** 判断Excel行是否仅包含空单元格或空白文本。 */
    private static boolean isBlank(Row row) {
        if (row == null) return true;
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.BLANK
                    && !(cell.getCellType() == CellType.STRING && cell.getStringCellValue().isBlank())) return false;
        }
        return true;
    }

    /** 按资产模板列解析数据并收集实际行号和字段错误。 */
    private static AssetSaveDTO parse(Row row, int rowNumber, List<RowErrorVO> errors) {
        AssetSaveDTO value = new AssetSaveDTO();
        for (int column = 0; column < HEADERS.length; column++) {
            try {
                Cell cell = row.getCell(column);
                String text = text(cell);
                switch (column) {
                    case 0: {
                        if (cell != null && cell.getCellType() != CellType.STRING && cell.getCellType() != CellType.BLANK) {
                            throw new IllegalArgumentException("资产编号必须使用文本单元格，避免前导零丢失");
                        }
                        value.setAssetCode(text);
                        break;
                    }
                    case 1: value.setAssetName(text); break;
                    case 2: value.setCategoryId(id(text)); break;
                    case 3: value.setSpecification(text); break;
                    case 4: {
                        if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                            value.setPurchaseDate(cell.getLocalDateTimeCellValue().toLocalDate());
                        } else value.setPurchaseDate(text.isEmpty() ? null : LocalDate.parse(text));
                        break;
                    }
                    case 5: value.setPurchasePrice(decimal(text)); break;
                    case 6: value.setSupplierName(text); break;
                    case 7: value.setExpectedUseYears(decimal(text)); break;
                    case 8: value.setDeptId(id(text)); break;
                    case 9: value.setLocationId(id(text)); break;
                    case 10: value.setKeeperUserId(id(text)); break;
                    case 11: value.setBorrowEnabled(text.isEmpty() ? "0" : text); break;
                    case 12: value.setVisibility(text.isEmpty() ? "1" : text); break;
                    case 13: value.setRemark(text); break;
                    default: throw new IllegalStateException("未知模板列");
                }
            } catch (DateTimeParseException e) {
                errors.add(new RowErrorVO(rowNumber, FIELDS[column], "日期格式必须为yyyy-MM-dd"));
            } catch (IllegalArgumentException e) {
                errors.add(new RowErrorVO(rowNumber, FIELDS[column], "单元格格式无效：" + e.getMessage()));
            }
        }
        if (row.getLastCellNum() > HEADERS.length) {
            errors.add(new RowErrorVO(rowNumber, "record", "存在模板以外的列"));
        }
        return value;
    }

    /** 读取文本或数值单元格，拒绝公式、布尔值及错误单元格。 */
    private static String text(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().strip();
            case NUMERIC: return org.apache.poi.ss.util.NumberToTextConverter.toText(cell.getNumericCellValue());
            default: throw new IllegalArgumentException("不接受公式、布尔值或错误单元格");
        }
    }

    /** 将非空单元格文本转换为关联记录ID。 */
    private static Long id(String text) {
        return text.isEmpty() ? null : Long.valueOf(text);
    }
    /** 将非空采购金额或年限文本转换为精确小数。 */
    private static BigDecimal decimal(String text) {
        return text.isEmpty() ? null : new BigDecimal(text);
    }

    /** 模板不生成任何业务测试行。 */
    @Override
    public void template(OutputStream output) throws IOException {
        write(java.util.Collections.emptyList(), output, false);
    }

    /** 导出明确字段，文本单元格避免将用户内容解释成Excel公式。 */
    @Override
    public void export(List<AssetVO> values, OutputStream output) throws IOException {
        write(values, output, true);
    }

    /** 写入模板表头或资产明细并冻结首行，输出Excel工作簿。 */
    private static void write(List<AssetVO> values, OutputStream output, boolean details) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(details ? "资产明细" : "资产导入");
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                header.createCell(i).setCellValue(HEADERS[i]);
                sheet.setColumnWidth(i, 22 * 256);
            }
            String[] extra = {"资产ID", "业务状态(0在库/1借出/2维修/3报废)", "完好状态(0正常/1损坏/2待核验)",
                "入册时间", "报废时间", "类别名称", "部门名称", "场所名称", "责任人"};
            if (details) {
                for (int i = 0; i < extra.length; i++) {
                    header.createCell(HEADERS.length + i).setCellValue(extra[i]);
                    sheet.setColumnWidth(HEADERS.length + i, 25 * 256);
                }
            }
            int rowNum = 1;
            for (AssetVO value : values) {
                Object[] cells = {value.getAssetCode(), value.getAssetName(), value.getCategoryId(),
                    value.getSpecification(), value.getPurchaseDate(), value.getPurchasePrice(),
                    value.getSupplierName(), value.getExpectedUseYears(), value.getDeptId(), value.getLocationId(),
                    value.getKeeperUserId(), value.getBorrowEnabled(), value.getVisibility(), value.getRemark(),
                    value.getAssetId(), value.getAssetStatus(), value.getConditionStatus(), value.getRegisteredTime(),
                    value.getScrappedTime(), value.getCategoryName(), value.getDeptName(), value.getLocationName(), value.getKeeperName()};
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < cells.length; i++) {
                    row.createCell(i, CellType.STRING).setCellValue(Objects.toString(cells[i], ""));
                }
            }
            sheet.createFreezePane(0, 1);
            workbook.write(output);
        }
    }
}
