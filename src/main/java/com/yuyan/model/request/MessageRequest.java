package com.yuyan.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lucky
 * @date 2024/1/15
 */
@Data
public class MessageRequest implements Serializable {
    private static final long serialVersionUID = 4795587022202690074L;

    /**
     * 接受消息用户id
     */
    private Long toId;

    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 消息内容
     */
    private String text;

    /**
     * 聊天类型 1-私聊 2-群聊
     */
    private Integer chatType;

    /**
     * 是否为管理员
     */
    private Boolean isAdmin;

}
