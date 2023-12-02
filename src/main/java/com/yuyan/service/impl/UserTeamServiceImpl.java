package com.yuyan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuyan.model.domain.UserTeam;
import com.yuyan.service.UserTeamService;
import com.yuyan.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author lucky
* @description 针对表【user_team(用户-队伍关系)】的数据库操作Service实现
* @createDate 2023-12-01 10:59:29
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




