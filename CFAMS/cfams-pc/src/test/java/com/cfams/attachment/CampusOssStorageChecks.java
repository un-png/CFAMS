package com.cfams.attachment;

import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectPermission;

/** 仅清理环境变量指定的本次测试对象，不枚举或删除其他对象。 */
public class CampusOssStorageChecks {

    /**
     * 验证环境变量指定的独立测试对象为私有对象，删除后确认清理结果。
     *
     * @param args 命令行参数，当前未使用
     */
    public static void main(String[] args) {
        String key = System.getenv("CFAMS_TEST_OBJECT_KEY");
        if (key == null || !key.matches("campus/asset/[a-f0-9-]{36}\\.png")) {
            throw new IllegalArgumentException("Invalid isolated test object key");
        }
        com.aliyun.oss.OSS client =
                new OSSClientBuilder()
                        .build(
                                System.getenv("CFAMS_OSS_ENDPOINT"),
                                System.getenv("CFAMS_OSS_ACCESS_KEY_ID"),
                                System.getenv("CFAMS_OSS_ACCESS_KEY_SECRET"));
        String bucket = System.getenv("CFAMS_OSS_BUCKET");
        try {
            if (!client.doesObjectExist(bucket, key)) {
                throw new IllegalStateException("Test object missing");
            }
            if (client.getObjectAcl(bucket, key).getPermission() != ObjectPermission.Private) {
                throw new IllegalStateException("Test object is not private");
            }
            client.deleteObject(bucket, key);
            if (client.doesObjectExist(bucket, key)) {
                throw new IllegalStateException("Test cleanup failed");
            }
            System.out.println("PASS private OSS object ACL and exact test-object cleanup");
        } finally {
            client.shutdown();
        }
    }
}
