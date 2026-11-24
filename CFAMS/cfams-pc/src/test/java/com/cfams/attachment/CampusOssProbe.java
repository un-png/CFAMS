package com.cfams.attachment;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;

/** 临时对象检查，只输出错误码并清理本次对象，禁止打印认证细节。 */
public class CampusOssProbe {

    /**
     * 使用环境变量配置检查OSS并上传临时私有对象，结束时清理本次上传对象。
     *
     * @param args 命令行参数，当前未使用
     */
    public static void main(String[] args) {
        com.aliyun.oss.OSS client =
                new OSSClientBuilder()
                        .build(
                                System.getenv("CFAMS_OSS_ENDPOINT"),
                                System.getenv("CFAMS_OSS_ACCESS_KEY_ID"),
                                System.getenv("CFAMS_OSS_ACCESS_KEY_SECRET"));
        String key = "campus/asset/" + java.util.UUID.randomUUID() + ".png";
        boolean written = false;
        try {
            System.out.println(
                    "bucketExists=" + client.doesBucketExist(System.getenv("CFAMS_OSS_BUCKET")));
            byte[] bytes =
                    java.util.Base64.getDecoder()
                            .decode(
                                    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jRZkAAAAASUVORK5CYII=");
            com.aliyun.oss.model.ObjectMetadata metadata =
                    new com.aliyun.oss.model.ObjectMetadata();
            metadata.setContentType("image/png");
            metadata.setContentLength(bytes.length);
            metadata.setObjectAcl(com.aliyun.oss.model.CannedAccessControlList.Private);
            client.putObject(
                    System.getenv("CFAMS_OSS_BUCKET"),
                    key,
                    new java.io.ByteArrayInputStream(bytes),
                    metadata);
            written = true;
            System.out.println("upload=true");
        } catch (OSSException error) {
            System.out.println("OSS_ERROR=" + error.getErrorCode());
            String raw = error.getRawResponseError();
            if (raw != null) {
                java.util.regex.Matcher actions =
                        java.util.regex.Pattern.compile("oss:[A-Za-z]+").matcher(raw);
                while (actions.find()) {
                    System.out.println("DENIED_ACTION=" + actions.group());
                }
                java.util.regex.Matcher messages =
                        java.util.regex.Pattern.compile("<Message>([^<]+)</Message>").matcher(raw);
                if (messages.find()) {
                    System.out.println("ERROR_REASON=" + messages.group(1));
                }
                java.util.regex.Matcher endpoint =
                        java.util.regex.Pattern.compile("<Endpoint>([^<]+)</Endpoint>")
                                .matcher(raw);
                if (endpoint.find()) {
                    System.out.println("EXPECTED_ENDPOINT=" + endpoint.group(1));
                }
            }
        } catch (ClientException error) {
            System.out.println("OSS_CLIENT_ERROR=" + error.getErrorCode());
        } catch (RuntimeException error) {
            System.out.println("LOCAL_ERROR=" + error.getClass().getName());
        } finally {
            if (written) {
                client.deleteObject(System.getenv("CFAMS_OSS_BUCKET"), key);
            }
            client.shutdown();
        }
    }
}
