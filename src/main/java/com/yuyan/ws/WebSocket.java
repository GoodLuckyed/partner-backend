package com.yuyan.ws;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.gson.Gson;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.yuyan.config.HttpSessionConfigurator;
import com.yuyan.constant.ChatTypeConstants;
import com.yuyan.constant.UserConstant;
import com.yuyan.model.domain.Chat;
import com.yuyan.model.domain.Follow;
import com.yuyan.model.domain.Team;
import com.yuyan.model.domain.User;
import com.yuyan.model.request.MessageRequest;
import com.yuyan.model.vo.MessageVo;
import com.yuyan.model.vo.WebSocketVo;
import com.yuyan.service.ChatService;
import com.yuyan.service.FollowService;
import com.yuyan.service.TeamService;
import com.yuyan.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author lucky
 * @date 2024/1/15
 */

@Slf4j
@Component
@ServerEndpoint(value = "/websocket/{userId}/{teamId}/{token}",configurator = HttpSessionConfigurator.class)
public class WebSocket{

    //存放队伍的连接信息
    public static final Map<String, ConcurrentHashMap<String,WebSocket>> ROOMS = new HashMap<>();

    //存储全局的连接信息 线程安全的无序集合
    public static final CopyOnWriteArraySet<Session> SESSIONS = new CopyOnWriteArraySet<>();

    //存放所有在线的客户端 (连接数)
    public static final Map<String,Session> SESSION_POOL = new HashMap<>(0);

    //和某个客户端连接对象，需要通过他来给客户端发送数据
    private Session session;

    //httpSession中存储着当前登录的用户信息
    private HttpSession httpSession;

    //用户的token
    private String token;


    //房间在线人数
    private static int onlineCount = 0;

    public static synchronized void addOnlineCount(){
        WebSocket.onlineCount++;
    }

    public static synchronized int getOnlineCount(){
        return onlineCount;
    }

    public static synchronized void subOnlineCount(){
        WebSocket.onlineCount--;
    }

    private static UserService userService;
    private static ChatService chatService;
    private static TeamService teamService;
    private static FollowService followService;

    @Resource
    public void setHeatMapService(UserService userService) {
        WebSocket.userService = userService;
    }
    @Resource
    public void setHeatMapService(ChatService chatService) {
        WebSocket.chatService = chatService;
    }
    @Resource
    public void setHeatMapService(TeamService teamService) {
        WebSocket.teamService = teamService;
    }
    @Resource
    public void setHeatMapService(FollowService followService) {
        WebSocket.followService = followService;
    }


    /**
     * 连接建立成功调用的方法
     * @param session
     * @param userId
     * @param teamId
     * @param config
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId, @PathParam("teamId") String teamId,@PathParam("token") String token , EndpointConfig config){
        if (StringUtils.isBlank(userId) || "undefined".equals(userId)) {
            sendError(userId,"参数有误");
            return;
        }
        this.token  = token;
        //获取httpsession 用户的信息
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        User user = (User) httpSession.getAttribute(UserConstant.USER_LOGIN_STATUS + token);
        if (user != null){
            this.httpSession = httpSession;
            this.session = session;
        }
        if (!"NaN".equals(teamId)){
            if (!ROOMS.containsKey(teamId)){
                ConcurrentHashMap<String, WebSocket> concurrentHashMap = new ConcurrentHashMap<>(0);
                concurrentHashMap.put(userId,this);
                ROOMS.put(String.valueOf(teamId),concurrentHashMap);
                //房间在线人数+1
                addOnlineCount();
            }else {
                if (!ROOMS.get(teamId).containsKey(userId)){
                    ROOMS.get(teamId).put(userId,this);
                    //房间在线人数+1
                    addOnlineCount();
                }
            }
            log.info("有新连接加入！当前在线人数为" + getOnlineCount());
        }else {
            SESSIONS.add(session);
            SESSION_POOL.put(userId,session);
            log.info("有新用户加入，userId={}, 当前在线人数为：{}", userId, SESSION_POOL.size());
            //通知所有的在线用户
            sendAllUsers();
        }
    }

    /**
     * 连接关闭调用的方法
     * @param session
     * @param teamId
     * @param userId
     */
    @OnClose
    public void OnClose(Session session,@PathParam("teamId") String teamId,@PathParam("userId") String userId){
        try {
            if (!"NaN".equals(teamId)){
                ROOMS.get(teamId).remove(userId);
                if (getOnlineCount() > 0){
                    subOnlineCount();
                }
                log.info("用户退出，当前在线人数为：{}", getOnlineCount());
            }else {
                if (!SESSION_POOL.isEmpty()){
                    SESSION_POOL.remove(userId);
                    SESSIONS.remove(session);
                }
                log.info("【WebSocket消息】连接断开，目前连接数为：" + SESSION_POOL.size());
                sendAllUsers();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收到消息时调用的方法
     * @param message
     * @param userId
     */
    @OnMessage
    public void onMessage(String message,@PathParam("userId") String userId){
        if ("PING".equals(message)){
            senfOneMessage(userId,"PONG");
            log.error("心跳,发送给:{},在线人数:{}",userId,getOnlineCount());
            return;
        }
        log.info("【WebSocket消息】收到客户端用户:{},发来的消息:{}",userId,message);
        MessageRequest messageRequest = new Gson().fromJson(message, MessageRequest.class);
        Long toId = messageRequest.getToId();
        Long teamId = messageRequest.getTeamId();
        String text = messageRequest.getText();
        Integer chatType = messageRequest.getChatType();
        User fromUser = userService.getById(userId);
        Team team = teamService.getById(teamId);
        if(chatType == ChatTypeConstants.PRIVATE_CHAT){
            //私聊
            privateChat(fromUser,toId,text,chatType);
        } else if (chatType == ChatTypeConstants.TEAM_CHAT) {
            //队伍聊天
            teamChat(fromUser,team,text,chatType);
        }else {
            //大厅聊天
            hallChat(fromUser,text,chatType);
        }
    }


    /**
     * 私聊
     * @param fromUser
     * @param toId
     * @param text
     * @param chatType
     */
    private void privateChat(User fromUser, Long toId, String text, Integer chatType) {
        //获取接收消息的用户websocket连接对象
        Session toSession = SESSION_POOL.get(toId.toString());
        if (toSession != null){
            //封装消息
            MessageVo messageVo = chatService.chatResult(fromUser.getId(), toId, text, chatType, DateUtil.date(System.currentTimeMillis()));
            //获取当前登录的用户信息
            User currentUser = (User) httpSession.getAttribute(UserConstant.USER_LOGIN_STATUS + token);
            if (currentUser.getId() == fromUser.getId()){
                messageVo.setIsMy(true);
            }
            //将消息格式化为json字符串
            String message = new Gson().toJson(messageVo);
            //发送消息给接收的用户
            senfOneMessage(toId.toString(),message);
            log.info("发送消息给用户:{},消息内容:{}",toId,message);
        }else {
            log.info("用户:{}不在线",toId);
        }
        //保存聊天记录
        saveChat(fromUser.getId(),toId,text,null,chatType);
        chatService.delCacheRecords(ChatTypeConstants.CACHE_CHAT_PRIVATE,fromUser.getId() + "" + toId);
        chatService.delCacheRecords(ChatTypeConstants.CACHE_CHAT_PRIVATE,toId + "" + fromUser.getId());
    }

    /**
     * 队伍聊天
     * @param fromUser
     * @param team
     * @param text
     * @param chatType
     */
    private void teamChat(User fromUser, Team team, String text, Integer chatType) {
        MessageVo messageVo = new MessageVo();
        WebSocketVo fromWebSocketVo = new WebSocketVo();
        BeanUtils.copyProperties(fromUser,fromWebSocketVo);
        messageVo.setFormUser(fromWebSocketVo);
        messageVo.setText(text);
        messageVo.setChatType(chatType);
        messageVo.setTeamId(team.getId());
        messageVo.setCreateTime(DateUtil.format(new Date(),"yyyy年MM月dd日 HH:mm:ss"));
        if (fromUser.getId() == team.getUserId() || fromUser.getUserRole() == UserConstant.ADMIN_ROLE){
            messageVo.setIsAdmin(true);
        }
        User currentUser = (User) httpSession.getAttribute(UserConstant.USER_LOGIN_STATUS + token);
        if (fromUser.getId() == currentUser.getId()){
            messageVo.setIsMy(true);
        }
        //消息转换为json字符串
        String message = new Gson().toJson(messageVo);
        try {
            broadcast(String.valueOf(team.getId()),message);
            //保存聊天记录
            saveChat(fromUser.getId(),null,text,team.getId(),chatType);
            chatService.delCacheRecords(ChatTypeConstants.CACHE_CHAT_TEAM,String.valueOf(team.getId()));
            log.info("队伍聊天,发送人:{},发送消息给队伍:{},消息内容:{}",fromUser.getId(),team.getId(),message);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    /**
     * 大厅聊天
     * @param fromUser
     * @param text
     * @param chatType
     */
    private void hallChat(User fromUser, String text, Integer chatType) {
        MessageVo messageVo = new MessageVo();
        WebSocketVo fromWebSocketVo = new WebSocketVo();
        BeanUtils.copyProperties(fromUser,fromWebSocketVo);
        messageVo.setFormUser(fromWebSocketVo);
        messageVo.setText(text);
        messageVo.setChatType(chatType);
        messageVo.setCreateTime(DateUtil.format(new Date(),"yyyy年MM月dd日 HH:mm:ss"));
        if (fromUser.getUserRole() == UserConstant.ADMIN_ROLE){
            messageVo.setIsAdmin(true);
        }
        User currentUser = (User) httpSession.getAttribute(UserConstant.USER_LOGIN_STATUS + token);
        if (fromUser.getId() == currentUser.getId()){
            messageVo.setIsMy(true);
        }
        String message = new Gson().toJson(messageVo);
        sendAllMessage(message);
        saveChat(fromUser.getId(),null,text,null,chatType);
        chatService.delCacheRecords(ChatTypeConstants.CACHE_CHAT_HALL,String.valueOf(fromUser.getId()));
    }


    /**
     * 保存聊天记录
     * @param fromId
     * @param toId
     * @param text
     * @param teamId
     * @param chatType
     */
    private void saveChat(Long fromId, Long toId, String text, Long teamId, Integer chatType) {
        if (chatType == ChatTypeConstants.PRIVATE_CHAT){
            LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Follow::getUserId,fromId).eq(Follow::getFollowUserId,toId);
            long count = followService.count(queryWrapper);
            if (count <= 0){
                sendError(String.valueOf(fromId),"尚未关注该用户");
                return;
            }
        }
        Chat chat = new Chat();
        chat.setFromId(fromId);
        chat.setText(text);
        chat.setChatType(chatType);
        if (toId != null && toId > 0){
            chat.setToId(toId);
        }
        if (teamId != null && teamId > 0){
            chat.setTeamId(teamId);
        }
        chatService.save(chat);
    }

    //发送信息给所有的在线用户
    private void sendAllUsers() {
        log.info("【WebSocket消息】发送所有在线用户信息");
        Map<String, List<WebSocketVo>> stringListHashMap = new HashMap<>();
        List<WebSocketVo> webSocketVos = new ArrayList<>();
        for (String key : SESSION_POOL.keySet()) {
            User user = userService.getById(key);
            WebSocketVo webSocketVo = new WebSocketVo();
            BeanUtils.copyProperties(user,webSocketVo);
            webSocketVos.add(webSocketVo);
        }
        stringListHashMap.put("users",webSocketVos);
        //广播消息
        sendAllMessage(JSONUtil.toJsonStr(stringListHashMap));
    }

    //发送广播消息
    private void sendAllMessage(String message) {
        log.info("【WebSocket消息】广播消息：" + message);
        for (Session session : SESSIONS) {
            try {
                if (session.isOpen()){
                    synchronized (session){
                        session.getBasicRemote().sendText(message);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //发送失败
    private void sendError(String userId, String errorMessage) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("error",errorMessage);
        senfOneMessage(userId,jsonObject.toString());
    }

    //发送单点消息
    private void senfOneMessage(String userId, String message) {
        Session session = SESSION_POOL.get(userId);
        if (session != null && session.isOpen()){
            try {
                synchronized (session){
                    log.info("【WebSocket消息】单点消息：" + message);
                    session.getAsyncRemote().sendText(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //队伍内群发消息
    private void broadcast(String teamId, String message) {
        ConcurrentHashMap<String, WebSocket> concurrentHashMap = ROOMS.get(teamId);
        for (String key : concurrentHashMap.keySet()) {
            try {
                WebSocket webSocket = concurrentHashMap.get(key);
                webSocket.sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //发送消息
    private void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }
}
