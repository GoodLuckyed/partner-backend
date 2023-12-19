package com.yuyan.service;

import com.yuyan.common.BaseResponse;
import com.yuyan.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuyan.model.domain.User;
import com.yuyan.model.dto.TeamQuery;
import com.yuyan.model.request.TeamJoinRequest;
import com.yuyan.model.request.TeamQuitRequest;
import com.yuyan.model.request.TeamUpdateRequest;
import com.yuyan.model.vo.TeamUserVo;

import java.util.List;

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

    /**
     *根据条件查询队伍
     * @param teamQuery
     * @return
     */
    List<TeamUserVo> listTeams(TeamQuery teamQuery,boolean isAdmin);

    /**
     * 修改队伍
     * @param teamUpdateRequest
     * @param currentUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User currentUser);

    /**
     * 用户加入队伍
     * @param teamJoinRequest
     * @param currentUser
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User currentUser);

    /**
     * 用户退出队伍
     * @param teamQuitRequest
     * @param currentUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User currentUser);

    /**
     * 删除（解散）队伍
     * @param teamId
     * @param currentUser
     * @return
     */
    boolean deleteTeam(Long teamId, User currentUser);
}
