package com.yuyan.model.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * @author lucky
 * @date 2023/12/30
 */
@Data
public class PostAddRequest implements Serializable {
    private static final long serialVersionUID = -8163157852034496741L;

    /**
     * 帖文标题
     */
    private String title;

    /**
     * 帖文内容
     */
    private String content;

    /**
     * 图片
     */
    private MultipartFile image;
}
