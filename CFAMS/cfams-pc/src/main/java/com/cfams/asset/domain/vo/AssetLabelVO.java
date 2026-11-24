package com.cfams.asset.domain.vo;

import lombok.Getter;
import lombok.Setter;

/** 资产标签定位数据 */
@Getter
@Setter
public class AssetLabelVO {
    private Long assetId;
    private String assetCode;
    private String assetName;
    private String qrToken;

    /** 创建空的资产标签定位数据，供请求绑定或逐项赋值使用。 */
    public AssetLabelVO() {
    }

    /** 使用完整字段创建资产标签定位数据。 */
    public AssetLabelVO(Long assetId, String assetCode, String assetName, String qrToken) {
        this.assetId = assetId;
        this.assetCode = assetCode;
        this.assetName = assetName;
        this.qrToken = qrToken;
    }

    /** 按全部字段比较资产标签定位数据，保持原值对象的相等语义。 */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        AssetLabelVO value = (AssetLabelVO) other;
        return java.util.Objects.equals(assetId, value.assetId) &&
                java.util.Objects.equals(assetCode, value.assetCode) &&
                java.util.Objects.equals(assetName, value.assetName) &&
                java.util.Objects.equals(qrToken, value.qrToken);
    }

    /** 根据全部字段计算哈希值，与值相等比较保持一致。 */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(assetId, assetCode, assetName, qrToken);
    }
}
