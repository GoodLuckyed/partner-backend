package com.yuyan.controller;

import com.yuyan.common.BaseResponse;
import com.yuyan.common.ErrorCode;
import com.yuyan.common.ResultUtils;
import com.yuyan.exception.BusinessException;
import com.yuyan.model.domain.User;
import com.yuyan.model.request.AddCommentRequest;
import com.yuyan.service.PostCommentsService;
import com.yuyan.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author lucky
 * @date 2024/1/2
 */

@RestController
@RequestMapping("/comments")
public class PostCommentsController {


    @Autowired
    private UserService userService;

    @Autowired
    private PostCommentsService postCommentsService;

    @ApiOperation("添加评论")
    @PostMapping("/add")
    public BaseResponse<String> addComment(@RequestBody AddCommentRequest addCommentRequest, HttpServletRequest request){
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null){
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        if (addCommentRequest.getPostId() == null || StringUtils.isBlank(addCommentRequest.getContent())){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        postCommentsService.addComment(addCommentRequest,currentUser.getId());
        return ResultUtils.success("添加成功");
    }
}
