package com.yuyan.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lucky
 * @date 2023/12/26
 */
@Data
public class NoticeAddRequest implements Serializable {
    private static final long serialVersionUID = -9026977323248805401L;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;
}
