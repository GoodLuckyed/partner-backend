package com.yuyan.controller;

import com.yuyan.common.BaseResponse;
import com.yuyan.common.ErrorCode;
import com.yuyan.common.ResultUtils;
import com.yuyan.exception.BusinessException;
import com.yuyan.model.domain.User;
import com.yuyan.model.vo.UserVo;
import com.yuyan.service.FollowService;
import com.yuyan.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author lucky
 * @date 2024/1/8
 */

@RestController
@RequestMapping("/follow")
public class FollowController {

    @Autowired
    private UserService userService;
    @Autowired
    private FollowService followService;

    @ApiOperation( "关注用户")
    @PostMapping("/add/{id}")
    public BaseResponse<String> followUser(@PathVariable("id") Long id, HttpServletRequest request){
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        followService.followUser(id,currentUser.getId());
        return ResultUtils.success("ok");
    }

    @ApiOperation("获取我关注的用户列表")
    @GetMapping("/list/follows")
    public BaseResponse<List<UserVo>> myFollow(HttpServletRequest request){
        User currentUser = userService.getCurrentUser(request);
        List<UserVo> userVoList = followService.myFollow(currentUser);
        return ResultUtils.success(userVoList);
    }

    @ApiOperation("获取粉丝列表")
    @GetMapping("/list/fans")
    public BaseResponse<List<UserVo>> myFans(HttpServletRequest request){
        User currentUser = userService.getCurrentUser(request);
        List<UserVo> userVoList = followService.myFans(currentUser);
        return ResultUtils.success(userVoList);
    }
}















