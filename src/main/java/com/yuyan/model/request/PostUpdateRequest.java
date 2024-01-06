package com.yuyan.model.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lucky
 * @date 2024/1/6
 */
@Data
public class PostUpdateRequest implements Serializable {
    private static final long serialVersionUID = -3508702577326193796L;
    /**
     * 帖文id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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
