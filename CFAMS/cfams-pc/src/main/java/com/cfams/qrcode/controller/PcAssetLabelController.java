package com.cfams.qrcode.controller;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.cfams.common.core.controller.BaseController;
import com.cfams.common.core.domain.AjaxResult;
import com.cfams.common.core.page.TableDataInfo;
import com.cfams.qrcode.domain.dto.AssetLabelBatchDTO;
import com.cfams.asset.service.CampusAssetService;
import com.cfams.qrcode.domain.dto.AssetLabelQueryDTO;
import com.cfams.asset.service.CampusQrService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

/** 标签数据及PNG接口，补打重用已有token。 */
@RestController
@RequestMapping("/campus/labels")
public class PcAssetLabelController extends BaseController {
    @Autowired
    private CampusAssetService campusAssetService;
    @Autowired
    private CampusQrService campusQrService;

    /** 返回用户可访问资产的指定尺寸二维码PNG。 */
    @PreAuthorize("@ss.hasAnyPermi('campus:labels:preview,campus:labels:print')")
    @GetMapping(value = "/{id}/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> image(@PathVariable Long id, @RequestParam(defaultValue = "256") int size) {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(campusQrService.png(id,size));
    }

    /** 计算数据范围后分页查询可打印标签的资产。 */
    @PreAuthorize("@ss.hasPermi('campus:labels:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetLabelQueryDTO query) {
        campusAssetService.scope(query);
        startPage();
        return getDataTable(campusAssetService.assets(query));
    }

    /** 返回批量资产标签预览所需的稳定定位数据。 */
    @PreAuthorize("@ss.hasPermi('campus:labels:preview')")
    @PostMapping("/preview")
    public AjaxResult preview(@Valid @RequestBody AssetLabelBatchDTO body) {
        return success(campusAssetService.labels(body.getAssetIds()));
    }

    /** 返回打印所需数据，实际渲染和打印由客户端承担。 */
    @PreAuthorize("@ss.hasPermi('campus:labels:print')")
    @PostMapping("/print")
    public AjaxResult print(@Valid @RequestBody AssetLabelBatchDTO body) {
        return success(campusAssetService.labels(body.getAssetIds()));
    }

    /** 仅PC授权查询入口，不替代未来的小程序身份与可见性规则。 */
    @PreAuthorize("@ss.hasPermi('campus:assets:query')")
    @GetMapping("/resolve/{token}")
    public AjaxResult resolve(@PathVariable String token) {
        return success(campusAssetService.resolve(token));
    }
}
