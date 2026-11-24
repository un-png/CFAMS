package com.cfams.attachment.service;

/** 业务服务契约；由对应实现类负责权限、事务和业务处理。 */
public interface CampusOssStorage {

    /**
     * 校验OSS配置后返回存储桶名称。
     *
     * @return 已配置的OSS存储桶名称
     */
    String bucket();

    /**
     * 以私有访问权限上传对象，转换OSS错误并始终释放客户端。
     *
     * @param key OSS对象键
     * @param bytes 文件内容字节
     * @param type 文件媒体类型
     */
    void put(String key, byte[] bytes, String type);

    /**
     * 删除指定OSS对象并始终释放客户端。
     *
     * @param key OSS对象键
     */
    void delete(String key);

    /**
     * 校验存储桶一致性并生成五分钟有效的私有对象签名地址。
     *
     * @param expectedBucket 附件记录的存储桶名称
     * @param key OSS对象键
     * @return 短期有效的附件签名地址
     */
    String signedUrl(String expectedBucket, String key);
}
