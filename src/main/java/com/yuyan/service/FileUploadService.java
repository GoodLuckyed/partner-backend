package com.yuyan.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author lucky
 * @date 2023/12/31
 */
public interface FileUploadService {

    /**
     * 上传图片
     * @param file
     * @return
     */
    String fileUpload(MultipartFile file) throws Exception;
}
