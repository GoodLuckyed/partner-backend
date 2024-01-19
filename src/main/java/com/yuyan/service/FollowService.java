package com.yuyan.service;

import com.yuyan.model.domain.Follow;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuyan.model.domain.User;
import com.yuyan.model.vo.UserVo;

import java.util.List;

/**
* @author lucky
* @description 针对表【follow】的数据库操作Service
* @createDate 2024-01-06 19:19:52
*/
public interface FollowService extends IService<Follow> {

    /**
     * 关注用户
     * @param id 关注用户id
     * @param userId 当前用户id
     */
    void followUser(Long id, Long userId);

    /**
     * 获取我关注的用户列表
     * @param currentUser
     * @return
     */
    List<UserVo> myFollow(User currentUser);

    /**
     * 获取粉丝列表
     * @param currentUser
     * @return
     */
    List<UserVo> myFans(User currentUser);
}
