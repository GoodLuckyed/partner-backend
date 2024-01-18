package com.yuyan.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lucky
 * @date 2024/1/15
 */
@Data
public class MessageVo implements Serializable {
    private static final long serialVersionUID = 143958857910515887L;
    /**
     *  发送消息用户
     */
    private WebSocketVo formUser;

    /**
     * 接受消息用户
     */
    private WebSocketVo toUser;

    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 消息内容
     */
    private String text;

    /**
     * 是否为自己发送的消息
     */
    private Boolean isMy = false;

    /**
     * 聊天类型 1-私聊 2-群聊
     */
    private Integer chatType;

    /**
     * 是否为管理员
     */
    private Boolean isAdmin = false;

    /**
     * 消息创建时间
     */
    private String createTime;

}
