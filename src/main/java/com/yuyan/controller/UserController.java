package com.yuyan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuyan.common.BaseResponse;
import com.yuyan.common.ErrorCode;
import com.yuyan.common.ResultUtils;
import com.yuyan.constant.UserConstant;
import com.yuyan.exception.BusinessException;
import com.yuyan.model.domain.Follow;
import com.yuyan.model.domain.User;
import com.yuyan.model.request.*;
import com.yuyan.model.vo.UserVo;
import com.yuyan.service.FollowService;
import com.yuyan.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户接口
 *
 * @author lucky
 * @date 2023/7/22 16:18
 */

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private FollowService followService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> useRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        String username = userRegisterRequest.getUsername();
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        long result = userService.userRegister(username,userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<String> useLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        String token = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(token);
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request){
        if (request == null){
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }
        int logout = userService.userLogout(request);
        return ResultUtils.success(logout);
    }

    /**
     * 获取当前登录用户信息
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        User safetyUser = userService.getCurrentUser(request);
        return ResultUtils.success(safetyUser);
    }

    /**
     * 用户查询
     *
     * @param username
     * @param request
     * @return
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNoneBlank(username)) {
            queryWrapper.like(User::getUsername, username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
            return ResultUtils.success(list);
    }

    /**
     * 推荐页面（主页接口）
     * @param request
     * @return
     */
    @GetMapping("/recommend")
    public BaseResponse<List<User>> recommend(long currentPage,long pageSize,HttpServletRequest request) {
        //创建分页对象
        Page<User> pageModel = new Page<>(currentPage, pageSize);
        //调用service查询
        Page<User> userPage = userService.recommend(pageModel,request);
        //获取查询结果
        List<User> userList = userPage.getRecords();
        //从查询结果中排除自己
        User currentUser = userService.getCurrentUser(request);
        userList = userList.stream().filter(user -> user.getId() != currentUser.getId()).collect(Collectors.toList());
        //脱敏
        userList = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(userList);
    }

    /**
     * 根据标签搜索用户
     * @param tagNameList
     * @return
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)){
           throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    /**
     * 修改用户信息
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user,HttpServletRequest request){
        //判断是否为空
        if (user == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        int result =  userService.updateUser(user,request);
        if (result < 1){
            throw new BusinessException(ErrorCode.EXECUTE_ERR);
        }
        return ResultUtils.success(result);
    }

    /**
     * 修改密码
     * @param userUpdatePassword
     * @param request
     * @return
     */
    @PostMapping("/update/password")
    public BaseResponse<Integer> updatePassword(@RequestBody UserUpdatePassword userUpdatePassword,HttpServletRequest request){
        if (userUpdatePassword == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        int updateTag = userService.updatePassword(userUpdatePassword,currentUser);
        if (updateTag < 0){
            throw new BusinessException(ErrorCode.EXECUTE_ERR);
        }
        //修改成功后清除登录态
        String token = request.getHeader("Authorization");
        redisTemplate.delete(UserConstant.USER_LOGIN_STATUS + token);
        return ResultUtils.success(updateTag);
    }

/**
     * 修改标签
     * @param updateTagRequest
     * @param request
     * @return
     */
    @PostMapping("/update/tags")
    public BaseResponse<Integer> updateTag(@RequestBody UpdateTagRequest updateTagRequest,HttpServletRequest request){
        if (updateTagRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        int updateTag = userService.updateTag(updateTagRequest,currentUser);
        if (updateTag < 0){
            throw new BusinessException(ErrorCode.EXECUTE_ERR);
        }
        return ResultUtils.success(updateTag);
    }

    /**
     * 上传头像
     * @param userAvatarRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadAvatar(UserAvatarRequest userAvatarRequest,HttpServletRequest request){
        if (userAvatarRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        userService.uploadAvatar(userAvatarRequest,currentUser);
        return ResultUtils.success("上传成功");
    }

    /**
     * 删除用户
     *
     * @param id
     * @param request
     * @return
     */
    @DeleteMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        if (id < 0) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 获取最匹配的用户
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);

        return ResultUtils.success(userService.matchUsers(num,currentUser));
    }

    /**
     * 根据userId获取用户信息
     */
    @GetMapping("/{id}")
    public BaseResponse<UserVo> getUserById(@PathVariable("id") Long id,HttpServletRequest request){
        if (id <= 0){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        if (currentUser == null){
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        User user = userService.getById(id);
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user,userVo);
        LambdaQueryWrapper<Follow> followLambdaQueryWrapper = new LambdaQueryWrapper<>();
        followLambdaQueryWrapper.eq(Follow::getUserId,currentUser.getId()).eq(Follow::getFollowUserId,id);
        long count = followService.count(followLambdaQueryWrapper);
        userVo.setIsFollow(count > 0);
        return ResultUtils.success(userVo);
    }
}
