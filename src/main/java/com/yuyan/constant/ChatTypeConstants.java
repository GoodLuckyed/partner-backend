package com.yuyan.constant;

/**
 * @author lucky
 * @date 2024/1/15
 */

/**
 * 聊天类常量
 */
public class ChatTypeConstants {

    //私聊
    public static final int PRIVATE_CHAT = 1;

    //队伍聊天
    public static final int TEAM_CHAT = 2;

    //大厅聊天
    public static final int HALL_CHAT = 3;

    public static final String CACHE_CHAT_PRIVATE = "partner:chat:chat_records:chat_private";

    public static final String CACHE_CHAT_TEAM = "partner:chat:chat_records:chat_team";

    public static final String CACHE_CHAT_HALL = "partner:chat:chat_records:chat_hall";
}
