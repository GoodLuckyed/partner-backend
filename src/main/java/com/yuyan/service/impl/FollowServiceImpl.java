package com.yuyan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuyan.model.domain.Follow;
import com.yuyan.model.domain.User;
import com.yuyan.model.vo.UserVo;
import com.yuyan.service.FollowService;
import com.yuyan.mapper.FollowMapper;
import com.yuyan.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author lucky
* @description 针对表【follow】的数据库操作Service实现
* @createDate 2024-01-06 19:19:52
*/
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService{

    @Autowired
    private UserService userService;

    /**
     * 关注用户
     * @param id 关注用户id
     * @param userId 当前用户id
     */
    @Override
    public void followUser(Long id, Long userId) {
        LambdaQueryWrapper<Follow> followLambdaQueryWrapper = new LambdaQueryWrapper<>();
        followLambdaQueryWrapper.eq(Follow::getFollowUserId, id).eq(Follow::getUserId, userId);
        long count = this.count(followLambdaQueryWrapper);
        if(count == 0){
            //没有关注过
            Follow follow = new Follow();
            follow.setFollowUserId(id);
            follow.setUserId(userId);
            this.save(follow);
        }else {
            //关注过了 取消关注
            this.remove(followLambdaQueryWrapper);
        }
    }

    /**
     * 获取我关注的用户列表
     * @param currentUser
     * @return
     */
    @Override
    public List<UserVo> myFollow(User currentUser) {
        Long userId = currentUser.getId();
        LambdaQueryWrapper<Follow> followLambdaQueryWrapper = new LambdaQueryWrapper<>();
        followLambdaQueryWrapper.eq(Follow::getUserId,userId);
        List<Follow> followList = this.list(followLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(followList)){
            return new ArrayList<>();
        }
        List<Long> idList = followList.stream().map(Follow::getFollowUserId).collect(Collectors.toList());
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.in(User::getId,idList);
        List<User> userList = userService.list(userLambdaQueryWrapper);
        List<UserVo> userVoList = userList.stream().map(user -> {
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(user, userVo);
            return userVo;
        }).collect(Collectors.toList());
        return userVoList;
    }

    /**
     * 获取粉丝列表
     * @param currentUser
     * @return
     */
    @Override
    public List<UserVo> myFans(User currentUser) {
        Long userId = currentUser.getId();
        LambdaQueryWrapper<Follow> followLambdaQueryWrapper = new LambdaQueryWrapper<>();
        followLambdaQueryWrapper.eq(Follow::getFollowUserId,userId);
        List<Follow> followList = this.list(followLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(followList)){
            return new ArrayList<>();
        }
        List<Long> idList = followList.stream().map(Follow::getUserId).collect(Collectors.toList());
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.in(User::getId,idList);
        List<User> userList = userService.list(userLambdaQueryWrapper);
        List<UserVo> userVoList = userList.stream().map(user -> {
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(user, userVo);
            return userVo;
        }).collect(Collectors.toList());
        return userVoList;
    }
}




