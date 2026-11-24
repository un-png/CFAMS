package com.cfams.identity.domain.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** 整批导入结果；任何失败时成功数为零，保留Excel实际行号。 */
@Getter
@Setter
public class PersonImportResultVO {
    private int successCount;
    private List<RowFailureVO> failures = new ArrayList<>();

    /** 人员导入行错误 */
    @Getter
    @Setter
    public static class RowFailureVO {
        private int row;
        private String reason;

        /** 创建空的人员导入行错误，供请求绑定或逐项赋值使用。 */
        public RowFailureVO() {
        }

        /** 使用完整字段创建人员导入行错误。 */
        public RowFailureVO(int row, String reason) {
            this.row = row;
            this.reason = reason;
        }

        /** 按全部字段比较人员导入行错误，保持原值对象的相等语义。 */
        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other == null || getClass() != other.getClass()) return false;
            RowFailureVO value = (RowFailureVO) other;
            return java.util.Objects.equals(row, value.row) &&
                    java.util.Objects.equals(reason, value.reason);
        }

        /** 根据全部字段计算哈希值，与值相等比较保持一致。 */
        @Override
        public int hashCode() {
            return java.util.Objects.hash(row, reason);
        }
    }
}
