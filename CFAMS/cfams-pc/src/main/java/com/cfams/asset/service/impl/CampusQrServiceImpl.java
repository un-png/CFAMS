package com.cfams.asset.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.cfams.asset.service.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import com.cfams.common.exception.ServiceException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/** 标签只编码稳定定位token，补打不重新分配标识。 */
@Service
public class CampusQrServiceImpl implements CampusQrService {
    @Autowired
    private CampusAssetService campusAssetService;

    /** 校验访问范围及图片尺寸，用资产既有定位标识生成二维码PNG。 */
    public byte[] png(Long assetId, int size) {
        if (size < 128 || size > 1024) throw new ServiceException("二维码尺寸须为128至1024像素");
        String token = campusAssetService.asset(assetId).getQrToken();
        try {
            com.google.zxing.common.BitMatrix matrix = new QRCodeWriter().encode(token, BarcodeFormat.QR_CODE, size, size,
                    Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M, EncodeHintType.MARGIN, 2));
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < size; x++) for (int y = 0; y < size; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xffffff);
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);
            return output.toByteArray();
        } catch (WriterException | IOException error) {
            throw new ServiceException("二维码生成失败");
        }
    }
}
