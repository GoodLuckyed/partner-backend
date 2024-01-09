package com.yuyan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuyan.model.domain.Follow;
import com.yuyan.service.FollowService;
import com.yuyan.mapper.FollowMapper;
import org.springframework.stereotype.Service;

/**
* @author lucky
* @description 针对表【follow】的数据库操作Service实现
* @createDate 2024-01-06 19:19:52
*/
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService{

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
}




