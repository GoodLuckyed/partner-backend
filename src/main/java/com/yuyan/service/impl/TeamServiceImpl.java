package com.yuyan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuyan.common.BaseResponse;
import com.yuyan.common.ErrorCode;
import com.yuyan.common.ResultUtils;
import com.yuyan.exception.BusinessException;
import com.yuyan.model.domain.Team;
import com.yuyan.model.domain.User;
import com.yuyan.model.domain.UserTeam;
import com.yuyan.model.dto.TeamQuery;
import com.yuyan.model.enums.TeamStatusEnum;
import com.yuyan.model.request.TeamJoinRequest;
import com.yuyan.model.request.TeamQuitRequest;
import com.yuyan.model.request.TeamUpdateRequest;
import com.yuyan.model.vo.TeamUserVo;
import com.yuyan.model.vo.UserVo;
import com.yuyan.service.TeamService;
import com.yuyan.mapper.TeamMapper;
import com.yuyan.service.UserService;
import com.yuyan.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author lucky
 * @description 针对表【team(队伍表)】的数据库操作Service实现
 * @createDate 2023-12-01 10:58:59
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {

    @Autowired
    private UserTeamService userTeamService;

    @Autowired
    private UserService userService;

    /**
     * 添加队伍
     *
     * @param team
     * @param currentUser 当前登录的用户
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public long addTeam(Team team, User currentUser) {
        //1. 请求参数是否为空？
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        //2. 是否登录，未登录不允许创建
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        final long userId = currentUser.getId();
        //3. 校验信息
        //a. 队伍人数 > 1 且 <= 20
        Integer maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum > 20 || maxNum < 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "队伍人数不满足要求");
        }
        //b. 队伍名字 <= 20
        String teamName = team.getName();
        if (StringUtils.isBlank(teamName) || teamName.length() > 20) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "队伍名字不满足要求");
        }
        //c. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "队伍描述信息过长");
        }
        //d. status 是否公开（int）不传默认为 0（公开）
        Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (teamStatusEnum == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "队伍状态不满足要求");
        }
        //e. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "密码设置不正确");
            }
        }
        //f. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前时间 > 超时时间");
        }
        //g. 校验用户最多创建 5 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long count = this.count(queryWrapper);
        if (count >= 5) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户最多创建5个队伍");
        }
        //4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ErrorCode.EXECUTE_ERR, "创建队伍失败");
        }
        //5. 插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.EXECUTE_ERR, "创建队伍失败");
        }
        return teamId;
    }

    /**
     * 根据条件查询队伍
     *
     * @param teamQuery
     * @return
     */
    @Override
    public List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        //组装查询条件
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            List<Long> idList = teamQuery.getIdList();
            if (!CollectionUtils.isEmpty(idList)) {
                queryWrapper.in("id", idList);
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("name", searchText));
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            //根据最大人数查询
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            //根据用户id查询
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            //根据队伍状态查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
            if (teamStatusEnum == null) {
                teamStatusEnum = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && !teamStatusEnum.equals(TeamStatusEnum.PUBLIC)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status", teamStatusEnum.getValue());
        }
        //不展示过期的队伍 expireTime is null or expireTime > now()
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));

        //查询队伍
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        //查询队伍关联创建人的用户信息 select * from team t left join user u on t.user_id = u.id
        List<TeamUserVo> teamUserVoList = new ArrayList<>();
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team, teamUserVo);
            User user = userService.getById(userId);
            if (user != null) {
                UserVo userVo = new UserVo();
                BeanUtils.copyProperties(user, userVo);
                teamUserVo.setCreateUser(userVo);
            }
            teamUserVoList.add(teamUserVo);
        }
        return teamUserVoList;
    }

    /**
     * 修改队伍
     *
     * @param teamUpdateRequest
     * @param currentUser
     * @return
     */
    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User currentUser) {
        //判断请求参数是否为空
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        Team oldTeam = getTeamById(id);
        //只有管理员或者队伍的创建者可以修改
        if (oldTeam.getUserId() != currentUser.getId() && !userService.isAdmin(currentUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "没有权限修改");
        }
        //如果队伍状态改为加密，必须要有密码
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if (teamStatusEnum.equals(TeamStatusEnum.SECRET)) {
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "密码不能为空");
            }
        }
        Team team = new Team();
        // 遍历teamUpdateRequest对象的所有属性
        for (Field field : teamUpdateRequest.getClass().getDeclaredFields()) {
            field.setAccessible(true); // 设置访问权限，允许访问私有属性
            try {
                // 如果属性类型是String并且值为空字符串，则设置该属性的值为null
                if (field.getType().equals(String.class) && "".equals(field.get(teamUpdateRequest))) {
                    field.set(teamUpdateRequest, null);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        BeanUtils.copyProperties(teamUpdateRequest, team);
        //更新队伍信息
        boolean result = this.updateById(team);
        return result;
    }

    /**
     * 用户加入队伍
     *
     * @param teamJoinRequest
     * @param currentUser
     * @return
     */
    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User currentUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        //队伍必须存在
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);
        //只能加入未过期的队伍
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.EXECUTE_ERR, "队伍已过期");
        }
        //禁止加入私有的队伍
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessException(ErrorCode.EXECUTE_ERR, "禁止加入私有队伍");
        }
        //如果加入的队伍是加密的，必须密码匹配才可以
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (StringUtils.isBlank(password) || !password.equals(teamJoinRequest.getPassword())) {
                throw new BusinessException(ErrorCode.EXECUTE_ERR, "密码错误");
            }
        }
        //用户最多加入 5 个队伍
        long userId = currentUser.getId();
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasJoinNum = userTeamService.count(queryWrapper);
        if (hasJoinNum > 5) {
            throw new BusinessException(ErrorCode.EXECUTE_ERR, "用户最多加入五个队伍");
        }
        //只能加入人数未满的队伍
        long teamCount = countTeamUserByTeamId(teamId);
        if (teamCount >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.EXECUTE_ERR, "队伍人数已满");
        }
        //不能重复加入已加入的队伍
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        queryWrapper.eq("userId", userId);
        long hasUserJoinTeam = userTeamService.count(queryWrapper);
        if (hasUserJoinTeam > 0) {
            throw new BusinessException(ErrorCode.EXECUTE_ERR, "用户已加入队伍");
        }

        //新增队伍 - 用户关联信息
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        boolean result = userTeamService.save(userTeam);
        return result;
    }



    /**
     * 用户退出队伍
     *
     * @param teamQuitRequest
     * @param currentUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User currentUser) {
        //1. 判断请求参数是否为空
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        //2. 判断队伍是否存在
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        //3.判断我是否已经加入队伍
        long userId = currentUser.getId();
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(userTeam);
        long count = userTeamService.count(queryWrapper);
        if(count <= 0) {
            throw new BusinessException(ErrorCode.EXECUTE_ERR,"用户未加入队伍");
        }
        //5.如果队伍只剩下一个人，队伍自动解散
        long teamJoinUserNum = this.countTeamUserByTeamId(teamId);
        if (teamJoinUserNum == 1){
            this.removeById(teamId);
        }else {
            //5.1 如果是队长退出，队伍自动转让给下一个人（最早加入的）
            if(team.getUserId() == userId){
                QueryWrapper<UserTeam> userTeamQueryWrapper =  new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId",teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if(CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1){
                    throw new BusinessException(ErrorCode.EXECUTE_ERR,"队伍信息异常");
                }
                UserTeam nextTeamUser = userTeamList.get(1);
                //5.3更新队长
                team = new Team();
                team.setId(teamId);
                team.setUserId(nextTeamUser.getUserId());
                boolean result = this.updateById(team);
                if (!result){
                    throw new BusinessException(ErrorCode.EXECUTE_ERR,"更新队长失败");
                }
            }
            //5.4 非队长退出，直接删除关联信息
        }
        return userTeamService.remove(queryWrapper);
    }


    /**
     * 删除（解散）队伍
     * @param teamId
     * @param currentUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(Long teamId, User currentUser) {
        //判队伍是否存在
        Team team = this.getTeamById(teamId);
        //判断是不是队伍的队长
        if(!team.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH,"没有权限访问");
        }
        //移除所有加入队伍的的关联信息
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(queryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.EXECUTE_ERR,"删除队伍失败");
        }
        //删除队伍
        return this.removeById(teamId);
    }

    /**
     * 根据队伍id查询队伍人数
     * @param teamId
     * @return
     */
    private long countTeamUserByTeamId(Long teamId) {
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        long teamCount = userTeamService.count(queryWrapper);
        return teamCount;
    }

    /**
     * 根据队伍id查询队伍信息
     * @param teamId
     * @return
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Team team = this.getById(teamId);
        if(team == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL,"队伍不存在");
        }
        return team;
    }

}

