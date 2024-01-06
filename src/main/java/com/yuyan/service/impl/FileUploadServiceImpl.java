package com.yuyan.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.yuyan.common.ErrorCode;
import com.yuyan.exception.BusinessException;
import com.yuyan.service.FileUploadService;
import lombok.Data;
import org.joda.time.DateTime;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author lucky
 * @date 2023/12/31
 */
@Service
@ConfigurationProperties(prefix = "aliyun")
@Data
public class FileUploadServiceImpl implements FileUploadService {

    private String endpoint;
    private String OSS_ACCESS_KEY_ID;
    private String OSS_ACCESS_KEY_SECRET;
    private String bucketName;

    // 最大图片大小 2M
    private final long maxFileSize = 2 * 1024 * 1024;

    /**
     * 上传图片
     * @param file
     * @return
     */
    @Override
    public String fileUpload(MultipartFile file) throws Exception{
        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
        // 获取原始文件名
        String objectName = file.getOriginalFilename();
        // 获取文件名后缀
        String suffix = objectName.substring(objectName.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        objectName = uuid + suffix;

        //按照当前日期，创建文件夹，上传到创建文件夹里面
        //  2023/01/01/01.jpg
        String currentTime = new DateTime().toString("yyyy/MM/dd");
        objectName = currentTime + "/" + objectName;


        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, OSS_ACCESS_KEY_ID, OSS_ACCESS_KEY_SECRET);
        //限制文件大小
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "图片大小不能超过2M");
        }

        try {
            InputStream inputStream = file.getInputStream();
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
            // 创建PutObject请求。
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            // 拼接上传后返回的文件路径
            String url = "https://" + bucketName + "." + endpoint + "/" + objectName;
            return url;
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        return null;
    }
}
