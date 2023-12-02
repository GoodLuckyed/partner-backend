package com.yuyan.service;

import com.yuyan.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuyan.model.domain.User;

/**
* @author lucky
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2023-12-01 10:58:59
*/
public interface TeamService extends IService<Team> {

    /**
     * 添加队伍
     * @param team
     * @param currentUser 当前登录的用户
     * @return
     */
    long addTeam(Team team, User currentUser);
}
