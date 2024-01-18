package com.yuyan.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lucky
 * @date 2024/1/15
 */
@Data
public class WebSocketVo implements Serializable {
    private static final long serialVersionUID = 4587243358312219112L;

    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;
}
