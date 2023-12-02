package com.yuyan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuyan.common.ErrorCode;
import com.yuyan.exception.BusinessException;
import com.yuyan.model.domain.Team;
import com.yuyan.model.domain.User;
import com.yuyan.model.domain.UserTeam;
import com.yuyan.model.enums.TeamStatusEnum;
import com.yuyan.service.TeamService;
import com.yuyan.mapper.TeamMapper;
import com.yuyan.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

/**
* @author lucky
* @description 针对表【team(队伍表)】的数据库操作Service实现
* @createDate 2023-12-01 10:58:59
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService{

    @Autowired
    private UserTeamService userTeamService;
    
    /**
     * 添加用户
     * @param team
     * @param currentUser 当前登录的用户
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public long addTeam(Team team, User currentUser) {
        //1. 请求参数是否为空？
        if (team == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        //2. 是否登录，未登录不允许创建
        if (currentUser == null){
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        final long userId = currentUser.getId();
        //3. 校验信息
        //a. 队伍人数 > 1 且 <= 20
        Integer maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum > 20 || maxNum < 1){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"队伍人数不满足要求");
        }
        //b. 队伍名字 <= 20
        String teamName = team.getName();
        if (StringUtils.isBlank(teamName) || teamName.length() > 20){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"队伍名字不满足要求");
        }
        //c. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) &&  description.length() > 512){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"队伍描述信息过长");
        }
        //d. status 是否公开（int）不传默认为 0（公开）
        Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (teamStatusEnum == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"队伍状态不满足要求");
        }
        //e. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)){
            if (StringUtils.isBlank(password) || password.length() > 32){
                throw new BusinessException(ErrorCode.PARAM_ERROR,"密码设置不正确");
            }
        }
        //f. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"当前时间 > 超时时间");
        }
        //g. 校验用户最多创建 5 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        long count = this.count(queryWrapper);
        if (count >= 5){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"用户最多创建5个队伍");
        }
        //4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null){
            throw new BusinessException(ErrorCode.EXECUTE_ERR,"创建队伍失败");
        }
        //5. 插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result){
            throw new BusinessException(ErrorCode.EXECUTE_ERR,"创建队伍失败");
        }
        return teamId;
    }
}




