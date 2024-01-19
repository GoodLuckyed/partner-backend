package com.yuyan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.yuyan.common.BaseResponse;
import com.yuyan.common.ErrorCode;
import com.yuyan.common.ResultUtils;
import com.yuyan.exception.BusinessException;
import com.yuyan.model.domain.Team;
import com.yuyan.model.domain.User;
import com.yuyan.model.domain.UserTeam;
import com.yuyan.model.dto.TeamQuery;
import com.yuyan.model.request.*;
import com.yuyan.model.vo.TeamUserVo;
import com.yuyan.model.vo.UserVo;
import com.yuyan.service.TeamService;
import com.yuyan.service.UserService;
import com.yuyan.service.UserTeamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lucky
 * @date 2023/12/1
 */
@RestController
@RequestMapping("/team")
@Api(tags = "队伍接口")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserTeamService userTeamService;
    @ApiOperation("添加队伍")
    @PostMapping("/addTeam")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long teamId = teamService.addTeam(team, currentUser);
        return ResultUtils.success(teamId);
    }

    @ApiOperation("修改队伍")
    @PostMapping("/updateTeam")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest, currentUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERR, "修改队伍失败");
        }
        return ResultUtils.success(true);
    }

    @ApiOperation("根据id查询队伍")
    @GetMapping("/getTeam")
    public BaseResponse<TeamUserVo> getTeamById(@RequestParam("id") Long id,HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }
        TeamUserVo teamUserVo = new TeamUserVo();
        BeanUtils.copyProperties(team,teamUserVo);
        //查询队伍的创建人信息
        User user = userService.getById(team.getUserId());
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        teamUserVo.setCreateUser(userVo);
        //查询队伍成员的用户信息
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getTeamId,team.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        List<Long> userIds = userTeamList.stream().map(UserTeam::getUserId).collect(Collectors.toList());
        //已加入队伍的人数
        teamUserVo.setHasJoinNum(userIds.size());
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.in(User::getId,userIds);
        List<UserVo> memberList = userService.list(userLambdaQueryWrapper).stream().map(user1 -> {
            UserVo userVo1 = new UserVo();
            BeanUtils.copyProperties(user1, userVo1);
            return userVo1;
        }).collect(Collectors.toList());
        teamUserVo.setMemberList(memberList);
        //判断当前查看队伍的用户是否加入队伍
        User currentUser = userService.getCurrentUser(request);
        LambdaQueryWrapper<UserTeam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTeam::getUserId,currentUser.getId()).eq(UserTeam::getTeamId,id);
        long count = userTeamService.count(wrapper);
        teamUserVo.setHasJoin(count > 0);
        return ResultUtils.success(teamUserVo);
    }

    @ApiOperation("根据条件查询队伍")
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVo>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        //1.查询队伍列表
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVo> teamList = teamService.listTeams(teamQuery, isAdmin);
        //2.判断是否已经加入队伍
        List<Long> teamIdList = teamList.stream().map(TeamUserVo::getId).collect(Collectors.toList());
        try {
            User currentUser = userService.getCurrentUser(request);
            QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userId",currentUser.getId());
            queryWrapper.in("teamId",teamIdList);
            //已加入的队伍列表
            List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
            List<Long> hasJoinTeamIdList = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toList());
            teamList.forEach(teamUserVo -> {
                if(hasJoinTeamIdList.contains(teamUserVo.getId())) {
                    teamUserVo.setHasJoin(true);
                }
            });
        } catch (Exception e) {
        }
        //查询已加入的队伍的人数
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.in("teamId",teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(teamUserVo -> {
            List<UserTeam> teamMembers  = teamIdUserTeamList.getOrDefault(teamUserVo.getId(), new ArrayList<>());
            teamUserVo.setHasJoinNum(teamMembers.size());
            //查询队伍成员的用户信息
            List<Long> memberUserIds  = teamMembers.stream().map(UserTeam::getUserId).collect(Collectors.toList());
            LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.in(User::getId,memberUserIds);
            List<User> userList = userService.list(userLambdaQueryWrapper);
            List<UserVo> memberList = userList.stream().map(user -> {
                UserVo userVo = new UserVo();
                BeanUtils.copyProperties(user, userVo);
                return userVo;
            }).collect(Collectors.toList());
            teamUserVo.setMemberList(memberList);
        });
        //按照创建时间倒序排序
        teamList.sort((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()));
        return ResultUtils.success(teamList);
    }

    @ApiOperation("查询我创建的队伍")
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVo>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        teamQuery.setUserId(currentUser.getId());
        List<TeamUserVo> teamList = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(teamList);
    }

    @ApiOperation("查询我加入的队伍")
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVo>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        Long userId = currentUser.getId();
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        // 获取我加入的队伍id列表
        List<Long> idList = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toList());
        teamQuery.setIdList(idList);
        List<TeamUserVo> teamList = teamService.listTeams(teamQuery, true);
        //设置是hasJoin为true
        teamList.stream().map(teamUserVo -> {
            teamUserVo.setHasJoin(true);
            return teamUserVo;
        }).collect(Collectors.toList());
        //查询已加入的人数
        queryWrapper = new QueryWrapper<>();
        queryWrapper.in("teamId",idList);
        userTeamList = userTeamService.list(queryWrapper);
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(teamUserVo -> {
            List<UserTeam> teamMembers  = teamIdUserTeamList.getOrDefault(teamUserVo.getId(), new ArrayList<>());
            teamUserVo.setHasJoinNum(teamMembers.size());
            //查询队伍成员的用户信息
            List<Long> memberUserIds  = teamMembers.stream().map(UserTeam::getUserId).collect(Collectors.toList());
            LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.in(User::getId,memberUserIds);
            List<User> userList = userService.list(userLambdaQueryWrapper);
            List<UserVo> memberList = userList.stream().map(user -> {
                UserVo userVo = new UserVo();
                BeanUtils.copyProperties(user, userVo);
                return userVo;
            }).collect(Collectors.toList());
            teamUserVo.setMemberList(memberList);
        });
        return ResultUtils.success(teamList);
    }

    @ApiOperation("分页查询")
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);
        Page<Team> teamPage = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> pageResult = teamService.page(teamPage, queryWrapper);
        return ResultUtils.success(pageResult);
    }

    @ApiOperation("用户加入队伍")
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest, currentUser);
        return ResultUtils.success(result);
    }

    @ApiOperation("用户退出队伍")
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if(teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, currentUser);
        return ResultUtils.success(result);
    }

    @ApiOperation("删除/解散队伍")
    @PostMapping("/deleteTeam")
    public BaseResponse<Boolean> deleteTeam(@RequestBody TeamDeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getTeamId() < 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        long teamId = deleteRequest.getTeamId();
        User currentUser = userService.getCurrentUser(request);
        boolean result = teamService.deleteTeam(teamId, currentUser);
        if (!result) {
            throw new BusinessException(ErrorCode.EXECUTE_ERR, "删除队伍失败");
        }
        return ResultUtils.success(true);
    }
}