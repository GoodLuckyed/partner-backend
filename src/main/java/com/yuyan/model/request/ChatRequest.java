package com.yuyan.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lucky
 * @date 2024/1/15
 */

@Data
public class ChatRequest implements Serializable {
    private static final long serialVersionUID = 6151156096697563895L;

    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 接收消息用户id
     */
    private Long toId;
}
