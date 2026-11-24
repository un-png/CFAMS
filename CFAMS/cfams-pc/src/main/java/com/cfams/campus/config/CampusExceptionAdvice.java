package com.cfams.campus.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.cfams.common.core.domain.AjaxResult;

/** 校园接口只返回可读约束错误，不回传SQL、表结构或完整请求内容。 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = {
    "com.cfams.asset.controller", "com.cfams.qrcode.controller", "com.cfams.borrow.controller",
    "com.cfams.repair.controller", "com.cfams.inventory.controller", "com.cfams.scrap.controller",
    "com.cfams.identity.controller", "com.cfams.statistics.controller", "com.cfams.attachment.controller",
    "com.cfams.message.controller"
})
public class CampusExceptionAdvice {
    /** 将编号或关联关系唯一约束冲突转换为可读提示。 */
    @ExceptionHandler(DuplicateKeyException.class)
    public AjaxResult duplicate(DuplicateKeyException error) {
        return AjaxResult.error("编号、绑定关系或业务记录已存在，请刷新核对");
    }
    /** 将字段长度、非空和关联约束失败转换为统一错误响应。 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public AjaxResult integrity(DataIntegrityViolationException error) {
        return AjaxResult.error("数据不符合字段或关联约束，请检查后重新提交");
    }
    /** 拒绝无法解析的请求体或不匹配的字段类型。 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public AjaxResult body(HttpMessageNotReadableException error) {
        return AjaxResult.error("请求格式或字段类型不正确");
    }
}
