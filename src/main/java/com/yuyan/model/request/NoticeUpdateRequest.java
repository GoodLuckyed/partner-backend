package com.yuyan.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lucky
 * @date 2023/12/26
 */
@Data
public class NoticeUpdateRequest implements Serializable {

    private static final long serialVersionUID = 954268055239139305L;

    /**
     * 公告id
     */
    private Long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;
}
