package com.cfams.asset.service;

/** 资产稳定二维码PNG生成业务契约。 */
public interface CampusQrService {
    /** 校验资产访问权限及尺寸，使用既有定位标识生成二维码PNG。 */
    byte[] png(Long assetId, int size);
}
