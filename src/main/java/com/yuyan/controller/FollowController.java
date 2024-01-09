package com.yuyan.controller;

import com.yuyan.common.BaseResponse;
import com.yuyan.common.ErrorCode;
import com.yuyan.common.ResultUtils;
import com.yuyan.exception.BusinessException;
import com.yuyan.model.domain.User;
import com.yuyan.service.FollowService;
import com.yuyan.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

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
}















