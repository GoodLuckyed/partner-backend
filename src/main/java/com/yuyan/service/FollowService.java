package com.yuyan.service;

import com.yuyan.model.domain.Follow;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
