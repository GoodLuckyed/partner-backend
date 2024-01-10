package com.yuyan.controller;



import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuyan.common.BaseResponse;
import com.yuyan.common.ErrorCode;
import com.yuyan.common.ResultUtils;
import com.yuyan.exception.BusinessException;
import com.yuyan.model.domain.User;
import com.yuyan.model.request.PostAddRequest;
import com.yuyan.model.request.PostUpdateRequest;
import com.yuyan.model.vo.PostVo;
import com.yuyan.service.PostService;
import com.yuyan.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

/**
 * @author lucky
 * @date 2023/12/30
 */

@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @ApiOperation("添加帖文")
    @PostMapping("/add")
    public BaseResponse<Boolean> addPost(PostAddRequest postAddRequest, HttpServletRequest request) {
        //判断用户是否登录
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        //参数校验
        if (postAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        //添加帖文
        boolean result = postService.addPost(postAddRequest, currentUser);
        if (!result) {
            throw new BusinessException(ErrorCode.EXECUTE_ERR, "添加失败");
        }
        return ResultUtils.success(true);
    }

    @ApiOperation("分页查询帖文")
    @GetMapping("/list")
    public BaseResponse<Page<PostVo>> listPostPage(long currentPage, String title, HttpServletRequest request) {
        //判断用户是否登录
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null) {
            return ResultUtils.success(postService.listPostPage(currentPage, title,null));
        }else {
            return ResultUtils.success(postService.listPostPage(currentPage, title,currentUser.getId()));
        }
    }

    @ApiOperation("根据id查询帖文")
    @GetMapping("/get/{id}")
    public BaseResponse<PostVo> getPostById(@PathVariable("id") Long id, HttpServletRequest request) {
        //判断用户是否登录
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null){
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        if (id <= 0){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return ResultUtils.success(postService.getPostById(id,currentUser.getId()));
    }

    @ApiOperation("根据id删除帖文")
    @DeleteMapping("/del/{id}")
    public BaseResponse<Boolean> deletePostById(@PathVariable("id") Long id, HttpServletRequest request) {
        //判断用户是否登录
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null){
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        if (id <= 0){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        //判断当前的用户是否是管理员
        boolean isAdmin = userService.isAdmin(currentUser);
        return ResultUtils.success(postService.deletePostById(id,currentUser.getId(),isAdmin));
    }

    @ApiOperation("更新帖文")
    @PutMapping("/update")
    public BaseResponse<Boolean> updatePost(PostUpdateRequest postUpdateRequest,HttpServletRequest request){
        if (postUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        //判断用户是否登录
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null){
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        //判断当前的用户是否是管理员
        boolean isAdmin = userService.isAdmin(currentUser);
        return ResultUtils.success(postService.updatePost(postUpdateRequest,currentUser.getId(),isAdmin));
    }

    @ApiOperation("点赞帖文")
    @PutMapping("/like/{id}")
    public BaseResponse<String> likePost(@PathVariable("id") Long id,HttpServletRequest request){
        //判断用户是否登录
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null){
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        if (id <= 0){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        postService.likePost(id,currentUser.getId());
        return ResultUtils.success("ok");
    }
}




















