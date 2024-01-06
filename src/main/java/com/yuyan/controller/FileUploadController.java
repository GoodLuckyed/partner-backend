package com.yuyan.controller;

import com.yuyan.common.BaseResponse;
import com.yuyan.common.ErrorCode;
import com.yuyan.common.ResultUtils;
import com.yuyan.exception.BusinessException;
import com.yuyan.service.FileUploadService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lucky
 * @date 2023/12/31
 */

@Api(tags = "图片上传")
@RestController
@RequestMapping("/file")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/upload")
    public BaseResponse<String> upload(MultipartFile file) throws Exception{
        if (file == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        //上传图片
        String imageUrl = fileUploadService.fileUpload(file);
        return ResultUtils.success(imageUrl);
    }
}
