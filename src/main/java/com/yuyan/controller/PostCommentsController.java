package com.yuyan.controller;

import com.yuyan.common.BaseResponse;
import com.yuyan.common.ErrorCode;
import com.yuyan.common.ResultUtils;
import com.yuyan.exception.BusinessException;
import com.yuyan.model.domain.User;
import com.yuyan.model.request.AddCommentRequest;
import com.yuyan.model.vo.PostCommentsVo;
import com.yuyan.service.PostCommentsService;
import com.yuyan.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
    public BaseResponse<String> addComment(@RequestBody AddCommentRequest addCommentRequest, HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        if (addCommentRequest.getPostId() == null || StringUtils.isBlank(addCommentRequest.getContent())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        postCommentsService.addComment(addCommentRequest, currentUser.getId());
        return ResultUtils.success("添加成功");
    }

    @ApiOperation("根据帖文id获取评论列表")
    @GetMapping("/list/{id}")
    public BaseResponse<List<PostCommentsVo>> listPostComments(@PathVariable("id") Long id, HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return ResultUtils.success(postCommentsService.listPostComments(id, currentUser.getId()));
    }

    @ApiOperation("根据评论id获取单个评论")
    @GetMapping("/get/{id}")
    public BaseResponse<PostCommentsVo> getCommentById(@PathVariable("id") Long id, HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return ResultUtils.success(postCommentsService.getCommentById(id, currentUser.getId()));
    }

    @ApiOperation("根据评论id删除评论")
    @DeleteMapping("/del/{id}")
    public BaseResponse<String> deleteCommentById(@PathVariable("id") Long id, HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        boolean isAdmin = userService.isAdmin(currentUser);
        postCommentsService.deleteComment(id, currentUser.getId(), isAdmin);
        return ResultUtils.success("删除成功");
    }

    @ApiOperation("点赞评论")
    @PutMapping("/like/{id}")
    public BaseResponse<String> likeComment(@PathVariable("id") Long id, HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        postCommentsService.likeComment(id, currentUser.getId());
        return ResultUtils.success("ok");
    }
}






















