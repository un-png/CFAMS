package com.cfams.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ca_asset 持久化实体。
 * 仅用于数据映射；外部写入必须使用独立白名单DTO。
 */
@Getter
@Setter
public class CaAsset {
    /** 主键 */
    public Long assetId;
    /** 终身唯一资产编号，注销后不复用 */
    public String assetCode;
    /** 资产名称 */
    public String assetName;
    /** 关联 ca_category */
    public Long categoryId;
    /** 品牌及规格型号文本 */
    public String specification;
    /** 采购日期；历史资产不详时为空 */
    public LocalDate purchaseDate;
    /** 登记采购金额，非负；0仅表示经确认的零金额 */
    public BigDecimal purchasePrice;
    /** 供应商名称文本，不引入供应商管理模块 */
    public String supplierName;
    /** 预计使用年限，大于0；未知为空 */
    public BigDecimal expectedUseYears;
    /** 本系统正式入册时间；历史统计起点 */
    public LocalDateTime registeredTime;
    /** 资产当前归属部门，关联 sys_dept */
    public Long deptId;
    /** 当前正式登记场所，关联 ca_location */
    public Long locationId;
    /** 台账责任人，关联 sys_user；区别于临时借用人 */
    public Long keeperUserId;
    /** 0在库；1借出；2维修中；3已报废 */
    public String assetStatus;
    /** 0正常；1损坏；2待核验 */
    public String conditionStatus;
    /** 0不开放借用；1开放借用；与实时可借判断不同 */
    public String borrowEnabled;
    /** 0校内已认证用户可见；1仅授权部门或关联人员可见 */
    public String visibility;
    /** 稳定且唯一的二维码定位标识，大小写敏感 */
    public String qrToken;
    /** 最终审核通过并注销的时间 */
    public LocalDateTime scrappedTime;
    /** 创建账号，服务端填写 */
    public String createBy;
    /** 创建时间，服务端填写 */
    public LocalDateTime createTime;
    /** 最后更新账号 */
    public String updateBy;
    /** 最后更新时间；不作为乐观锁版本 */
    public LocalDateTime updateTime;
    /** 补充说明，不替代结构化业务字段 */
    public String remark;

}
