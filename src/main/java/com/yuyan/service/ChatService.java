package com.yuyan.service;
import com.yuyan.model.domain.Chat;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuyan.model.domain.User;
import com.yuyan.model.request.ChatRequest;
import com.yuyan.model.vo.MessageVo;

import java.util.Date;
import java.util.List;

/**
* @author lucky
* @description 针对表【chat】的数据库操作Service
* @createDate 2024-01-13 00:10:14
*/
public interface ChatService extends IService<Chat> {


    /**
     * 聊天记录封装
     * @param fromId
     * @param toId
     * @param text
     * @param chatType
     * @param date
     */
    MessageVo chatResult(Long fromId, Long toId, String text, Integer chatType, Date date);

    /**
     * 获取私聊聊天记录
     * @param chatRequest
     * @param chatType
     * @param currentUser
     * @return
     */
    List<MessageVo> getPrivateChat(ChatRequest chatRequest, int chatType, User currentUser);

    /**
     * 获取队伍聊天记录
     * @param chatRequest
     * @param chatType
     * @param currentUser
     * @return
     */
    List<MessageVo> getTeamChat(ChatRequest chatRequest, int chatType, User currentUser);

    /**
     * 获取大厅聊天记录
     * @param chatType
     * @param currentUser
     * @return
     */
    List<MessageVo> getHallChat(int chatType, User currentUser);

    /**
     * 清除缓存
     * @param redisKey
     * @param id
     */
    void delCacheRecords(String redisKey,String id);
}
