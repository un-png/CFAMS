package com.cfams.asset.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import com.cfams.asset.domain.vo.AssetVO;
import com.cfams.asset.domain.vo.AssetImportResultVO;

/** 资产模板、整批导入与明细导出业务契约。 */
public interface CampusAssetExcelService {
    int MAX_ROWS = 2000;
    /** 逐行校验资产模板，整批通过后在同一事务内入册并返回行错误。 */
    AssetImportResultVO importWorkbook(InputStream input) throws IOException;
    /** 输出资产导入表头模板且不生成业务数据行。 */
    void template(OutputStream output) throws IOException;
    /** 按台账筛选结果导出明细，文本单元格防止内容被解释为公式。 */
    void export(List<AssetVO> values, OutputStream output) throws IOException;
}
