package com.cfams.attachment.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import com.cfams.attachment.service.CampusOssStorage;
import com.cfams.common.exception.ServiceException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.Date;

/** OSS私有文件存储；没有配置时失败，不回退到公开本地路径。 */
@Service
public class CampusOssStorageImpl implements CampusOssStorage {

    @Value("${campus.oss.endpoint:${alioss.endpoint:${CFAMS_OSS_ENDPOINT:}}}")
    private String endpoint;

    @Value("${campus.oss.access-key-id:${alioss.access-key-id:${CFAMS_OSS_ACCESS_KEY_ID:}}}")
    private String accessKeyId;

    @Value(
            "${campus.oss.access-key-secret:${alioss.access-key-secret:${CFAMS_OSS_ACCESS_KEY_SECRET:}}}")
    private String accessKeySecret;

    @Value("${campus.oss.bucket:${alioss.bucket-name:${CFAMS_OSS_BUCKET:}}}")
    private String bucket;

    /**
     * 校验OSS配置后返回存储桶名称。
     *
     * @return 已配置的OSS存储桶名称
     */
    @Override
    public String bucket() {
        configured();
        return bucket;
    }

    /** 校验OSS连接和凭证配置完整性，未配置时拒绝存储操作。 */
    private void configured() {
        if (endpoint.isBlank()
                || accessKeyId.isBlank()
                || accessKeySecret.isBlank()
                || bucket.isBlank()) {
            throw new ServiceException("OSS尚未配置，附件未上传");
        }
    }

    /**
     * 校验配置后创建使用当前凭证的OSS客户端。
     *
     * @return 需由调用方关闭的OSS客户端
     */
    private OSS client() {
        configured();
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    /**
     * 以私有访问权限上传对象，转换OSS错误并始终释放客户端。
     *
     * @param key OSS对象键
     * @param bytes 文件内容字节
     * @param type 文件媒体类型
     */
    @Override
    public void put(String key, byte[] bytes, String type) {
        OSS client = client();
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(type);
            metadata.setContentLength(bytes.length);
            metadata.setObjectAcl(CannedAccessControlList.Private);
            client.putObject(bucket, key, new ByteArrayInputStream(bytes), metadata);
        } catch (OSSException error) {
            throw new ServiceException("OSS上传失败：" + error.getErrorCode());
        } catch (ClientException error) {
            throw new ServiceException("OSS连接失败：" + error.getErrorCode());
        } catch (RuntimeException error) {
            throw new ServiceException("OSS上传失败，请检查存储配置");
        } finally {
            client.shutdown();
        }
    }

    /**
     * 删除指定OSS对象并始终释放客户端。
     *
     * @param key OSS对象键
     */
    @Override
    public void delete(String key) {
        OSS client = client();
        try {
            client.deleteObject(bucket, key);
        } finally {
            client.shutdown();
        }
    }

    /**
     * 校验存储桶一致性并生成五分钟有效的私有对象签名地址。
     *
     * @param expectedBucket 附件记录的存储桶名称
     * @param key OSS对象键
     * @return 短期有效的附件签名地址
     */
    @Override
    public String signedUrl(String expectedBucket, String key) {
        if (!bucket().equals(expectedBucket)) {
            throw new ServiceException("附件存储桶与当前配置不一致");
        }
        OSS client = client();
        try {
            return client.generatePresignedUrl(
                            bucket, key, new Date(System.currentTimeMillis() + 300_000))
                    .toString();
        } catch (RuntimeException error) {
            throw new ServiceException("生成附件访问地址失败");
        } finally {
            client.shutdown();
        }
    }
}
