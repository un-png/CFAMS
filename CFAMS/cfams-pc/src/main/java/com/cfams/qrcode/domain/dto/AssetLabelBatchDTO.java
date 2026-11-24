package com.cfams.qrcode.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import jakarta.validation.constraints.*;

/** 标签批量请求 */
@Getter
@Setter
public class AssetLabelBatchDTO {
    @NotEmpty
    @Size(max = 200)
    private List<@NotNull @Positive Long> assetIds;

    /** 创建空的标签批量请求，供请求绑定或逐项赋值使用。 */
    public AssetLabelBatchDTO() {
    }

    /** 使用完整字段创建标签批量请求。 */
    public AssetLabelBatchDTO(List<@NotNull @Positive Long> assetIds) {
        this.assetIds = assetIds;
    }

    /** 按全部字段比较标签批量请求，保持原值对象的相等语义。 */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        AssetLabelBatchDTO value = (AssetLabelBatchDTO) other;
        return java.util.Objects.equals(assetIds, value.assetIds);
    }

    /** 根据全部字段计算哈希值，与值相等比较保持一致。 */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(assetIds);
    }
}
