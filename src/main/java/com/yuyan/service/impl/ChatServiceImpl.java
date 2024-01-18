package com.yuyan.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuyan.common.ErrorCode;
import com.yuyan.constant.ChatTypeConstants;
import com.yuyan.constant.UserConstant;
import com.yuyan.exception.BusinessException;
import com.yuyan.model.domain.Chat;
import com.yuyan.mapper.ChatMapper;
import com.yuyan.model.domain.Team;
import com.yuyan.model.domain.User;
import com.yuyan.model.request.ChatRequest;
import com.yuyan.model.vo.MessageVo;
import com.yuyan.model.vo.WebSocketVo;
import com.yuyan.service.ChatService;
import com.yuyan.service.TeamService;
import com.yuyan.service.UserService;
import org.apache.xmlbeans.impl.common.NameUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author lucky
* @description 针对表【chat】的数据库操作Service实现
* @createDate 2024-01-13 00:10:14
*/
@Service
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat> implements ChatService{

    @Autowired
    private UserService userService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取私聊聊天记录
     * @param chatRequest
     * @param chatType
     * @param currentUser
     * @return
     */
    @Override
    public List<MessageVo> getPrivateChat(ChatRequest chatRequest, int chatType, User currentUser) {
        Long toId = chatRequest.getToId();
        if(toId == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        //从缓存中获取聊天记录
        List<MessageVo> chatRecords =  getCacheRecords(ChatTypeConstants.CACHE_CHAT_PRIVATE,currentUser.getId()+""+toId);
        if (chatRecords != null){
            //重新放入缓存,刷新缓存过期时间
            setCacheRecords(ChatTypeConstants.CACHE_CHAT_PRIVATE,currentUser.getId()+""+toId,chatRecords);
            return chatRecords;
        }
        //从数据库中获取聊天记录
        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.eq(Chat::getFromId,currentUser.getId()).eq(Chat::getToId,toId)
                .or().eq(Chat::getFromId,toId).eq(Chat::getToId,currentUser.getId())
                .eq(Chat::getChatType,chatType);
        List<Chat> chatList = this.list(chatLambdaQueryWrapper);
        List<MessageVo> messageVoList = chatList.stream().map(chat -> {
            MessageVo messageVo = chatResult(currentUser.getId(), toId, chat.getText(), chatType, chat.getCreateTime());
            if (chat.getFromId().equals(currentUser.getId())) {
                messageVo.setIsMy(true);
            }
            return messageVo;
        }).collect(Collectors.toList());
        //将聊天记录放入缓存
        setCacheRecords(ChatTypeConstants.CACHE_CHAT_PRIVATE,currentUser.getId()+""+toId,messageVoList);
        return messageVoList;
    }

    /**
     * 获取队伍聊天记录
     * @param chatRequest
     * @param chatType
     * @param currentUser
     * @return
     */
    @Override
    public List<MessageVo> getTeamChat(ChatRequest chatRequest, int chatType, User currentUser) {
        Long teamId = chatRequest.getTeamId();
        if (teamId == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        //从缓存中获取聊天记录
        List<MessageVo> chatRecords = getCacheRecords(ChatTypeConstants.CACHE_CHAT_TEAM, String.valueOf(teamId));
        if (chatRecords != null){
            //判断消息是否是自己的发的
            List<MessageVo> messageVos = checkIsMyMessage(currentUser,chatRecords);
            //保存到缓存中
            setCacheRecords(ChatTypeConstants.CACHE_CHAT_TEAM,String.valueOf(teamId),messageVos);
            return messageVos;
        }
        //从数据库查询
        Team team = teamService.getById(teamId);
        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.eq(Chat::getChatType,chatType).eq(Chat::getTeamId,teamId);
        List<MessageVo> messageVos = returnMessage(currentUser, team.getUserId(), chatLambdaQueryWrapper);
        //保存到缓存
        setCacheRecords(ChatTypeConstants.CACHE_CHAT_TEAM,String.valueOf(teamId),messageVos);
        return messageVos;
    }

    /**
     * messageVos
     * @param chatType
     * @param currentUser
     * @return
     */
    @Override
    public List<MessageVo> getHallChat(int chatType, User currentUser) {
        //从缓存中获取聊天记录
        List<MessageVo> chatRecords = getCacheRecords(ChatTypeConstants.CACHE_CHAT_HALL, String.valueOf(currentUser.getId()));
        if (chatRecords != null){
            //判断消息是否是自己的发的
            List<MessageVo> messageVos = checkIsMyMessage(currentUser, chatRecords);
            //保存到缓存中
            setCacheRecords(ChatTypeConstants.CACHE_CHAT_HALL,String.valueOf(currentUser.getId()),messageVos);
            return messageVos;
        }
        LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatLambdaQueryWrapper.eq(Chat::getChatType,chatType);
        List<MessageVo> messageVos = returnMessage(currentUser, null, chatLambdaQueryWrapper);
        setCacheRecords(ChatTypeConstants.CACHE_CHAT_HALL,String.valueOf(currentUser.getId()),messageVos);
        return messageVos;
    }

    private List<MessageVo> returnMessage(User currentUser, Long userId, LambdaQueryWrapper<Chat> chatLambdaQueryWrapper) {
        List<Chat> chatList = this.list(chatLambdaQueryWrapper);
        return chatList.stream().map(chat -> {
            MessageVo messageVo = chatResult(chat.getFromId(), chat.getText(),chat.getChatType(),chat.getCreateTime());
            boolean isCaptain = userId != null && userId.equals(chat.getFromId());
            if(userService.getById(chat.getFromId()).getUserRole() == UserConstant.ADMIN_ROLE || isCaptain){
                messageVo.setIsAdmin(true);
            }
            if (chat.getFromId().equals(currentUser.getId())){
                messageVo.setIsMy(true);
            }
            return messageVo;
        }).collect(Collectors.toList());
    }

    //判断消息是否是自己的发的
    private List<MessageVo> checkIsMyMessage(User currentUser, List<MessageVo> chatRecords) {
        return chatRecords.stream().peek(chat -> {
            if(!chat.getFormUser().getId().equals(currentUser.getId()) && chat.getIsMy()){
                chat.setIsMy(false);
            }
            if (chat.getFormUser().getId().equals(currentUser.getId()) && !chat.getIsMy()){
                chat.setIsMy(true);
            }
        }).collect(Collectors.toList());
    }

    public MessageVo chatResult(Long userId,String text,Integer chatType,Date date){
        MessageVo messageVo = new MessageVo();
        User fromUser = userService.getById(userId);
        WebSocketVo fromWebSocketVo = new WebSocketVo();
        BeanUtils.copyProperties(fromUser,fromWebSocketVo);
        messageVo.setFormUser(fromWebSocketVo);
        messageVo.setText(text);
        messageVo.setChatType(chatType);
        messageVo.setCreateTime(DateUtil.format(date,"yyyy年MM月dd日 HH:mm:ss"));
        return messageVo;
    }

    /**
     * 聊天记录封装
     * @param fromId
     * @param toId
     * @param text
     * @param chatType
     * @param date
     */
    @Override
    public MessageVo chatResult(Long fromId, Long toId, String text, Integer chatType, Date date) {
        MessageVo messageVo = new MessageVo();
        User fromUser = userService.getById(fromId);
        User toUser = userService.getById(toId);
        WebSocketVo fromWebSocketVo = new WebSocketVo();
        WebSocketVo toWebSocketVo = new WebSocketVo();
        BeanUtils.copyProperties(fromUser,fromWebSocketVo);
        BeanUtils.copyProperties(toUser,toWebSocketVo);
        messageVo.setFormUser(fromWebSocketVo);
        messageVo.setToUser(toWebSocketVo);
        messageVo.setText(text);
        messageVo.setChatType(chatType);
        messageVo.setCreateTime(DateUtil.format(date,"yyyy年MM月dd日 HH:mm:ss"));
        return messageVo;
    }

    //将聊天记录放入缓存
    private void setCacheRecords(String redisKey, String id, List<MessageVo> chatRecords) {
        try {
            ValueOperations<String,List<MessageVo>> valueOperations = redisTemplate.opsForValue();
            //给过期的时间添加随机值,解决缓存雪崩
            int i = RandomUtil.randomInt(1, 5);
            if (redisKey.equals(ChatTypeConstants.CACHE_CHAT_HALL)){
                valueOperations.set(redisKey,chatRecords,2 + i / 10, TimeUnit.MINUTES);
            }else {
                valueOperations.set(redisKey+id,chatRecords,2 + i / 10, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.error("redis set key error");
        }
    }

    //从缓存中获取聊天记录
    private List<MessageVo> getCacheRecords(String redisKey, String id) {
        ValueOperations<String,List<MessageVo>> valueOperations = redisTemplate.opsForValue();
        List<MessageVo> charRecords = null;
        if (redisKey.equals(ChatTypeConstants.CACHE_CHAT_HALL)){
            charRecords =  valueOperations.get(redisKey);
        }else {
            charRecords = valueOperations.get(redisKey+id);
        }
        return charRecords;
    }

    //清除缓存
    @Override
    public void delCacheRecords(String redisKey,String id){
        if (redisKey.equals(ChatTypeConstants.CACHE_CHAT_HALL)){
            redisTemplate.delete(redisKey);
        }else {
            redisTemplate.delete(redisKey + id);
        }
    }
}




