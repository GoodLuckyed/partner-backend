package com.yuyan.controller;

import com.yuyan.common.BaseResponse;
import com.yuyan.common.ErrorCode;
import com.yuyan.common.ResultUtils;
import com.yuyan.constant.ChatTypeConstants;
import com.yuyan.exception.BusinessException;
import com.yuyan.model.domain.User;
import com.yuyan.model.request.ChatRequest;
import com.yuyan.model.vo.MessageVo;
import com.yuyan.service.ChatService;
import com.yuyan.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author lucky
 * @date 2024/1/13
 */

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private UserService userService;

    @Autowired
    private ChatService chatService;

    @ApiOperation("获取私聊聊天记录")
    @PostMapping("/privateChat")
    public BaseResponse<List<MessageVo>> getPrivateChat(@RequestBody ChatRequest chatRequest, HttpServletRequest request){
        if (chatRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        List<MessageVo> privateMessageVo =  chatService.getPrivateChat(chatRequest, ChatTypeConstants.PRIVATE_CHAT, currentUser);
        return ResultUtils.success(privateMessageVo);
    }

    @ApiOperation("获取队伍聊天记录")
    @PostMapping("/teamChat")
    public BaseResponse<List<MessageVo>> getTeamChat(@RequestBody ChatRequest chatRequest,HttpServletRequest request){
        if (chatRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        List<MessageVo> teamMessageVo = chatService.getTeamChat(chatRequest, ChatTypeConstants.TEAM_CHAT, currentUser);
        return ResultUtils.success(teamMessageVo);
    }

    @ApiOperation("获取大厅聊天记录")
    @GetMapping("/hallChat")
    public BaseResponse<List<MessageVo>> getHallChat(HttpServletRequest request){
        User currentUser = userService.getCurrentUser(request);
        List<MessageVo> hallMessageVo = chatService.getHallChat(ChatTypeConstants.HALL_CHAT, currentUser);
        return ResultUtils.success(hallMessageVo);
    }
}
