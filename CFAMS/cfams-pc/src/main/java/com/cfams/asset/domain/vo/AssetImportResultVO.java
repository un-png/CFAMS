package com.cfams.asset.domain.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** 资产整批导入结果 */
@Getter
@Setter
public class AssetImportResultVO {
    private int total;
    private int imported;
    private List<RowErrorVO> errors;

    /** 创建空的资产整批导入结果，供请求绑定或逐项赋值使用。 */
    public AssetImportResultVO() {
    }

    /** 使用完整字段创建资产整批导入结果。 */
    public AssetImportResultVO(int total, int imported, List<RowErrorVO> errors) {
        this.total = total;
        this.imported = imported;
        this.errors = errors;
    }

    /** 按全部字段比较资产整批导入结果，保持原值对象的相等语义。 */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        AssetImportResultVO value = (AssetImportResultVO) other;
        return java.util.Objects.equals(total, value.total) &&
                java.util.Objects.equals(imported, value.imported) &&
                java.util.Objects.equals(errors, value.errors);
    }

    /** 根据全部字段计算哈希值，与值相等比较保持一致。 */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(total, imported, errors);
    }

    /** 资产导入行错误 */
    @Getter
    @Setter
    public static class RowErrorVO {
        private int row;
        private String field;
        private String reason;

        /** 创建空的资产导入行错误，供请求绑定或逐项赋值使用。 */
        public RowErrorVO() {
        }

        /** 使用完整字段创建资产导入行错误。 */
        public RowErrorVO(int row, String field, String reason) {
            this.row = row;
            this.field = field;
            this.reason = reason;
        }

        /** 按全部字段比较资产导入行错误，保持原值对象的相等语义。 */
        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other == null || getClass() != other.getClass()) return false;
            RowErrorVO value = (RowErrorVO) other;
            return java.util.Objects.equals(row, value.row) &&
                    java.util.Objects.equals(field, value.field) &&
                    java.util.Objects.equals(reason, value.reason);
        }

        /** 根据全部字段计算哈希值，与值相等比较保持一致。 */
        @Override
        public int hashCode() {
            return java.util.Objects.hash(row, field, reason);
        }
    }
}
