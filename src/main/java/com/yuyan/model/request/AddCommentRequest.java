package com.yuyan.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lucky
 * @date 2024/1/3
 */
@Data
public class AddCommentRequest implements Serializable {
    private static final long serialVersionUID = 8998426220634946911L;

    /**
     * 帖文id
     */
    private Long postId;

    /**
     * 回复的内容
     */
    private String content;

}
