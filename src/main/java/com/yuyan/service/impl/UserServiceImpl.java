package com.yuyan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yuyan.common.ErrorCode;
import com.yuyan.common.ResultUtils;
import com.yuyan.constant.UserConstant;
import com.yuyan.exception.BusinessException;
import com.yuyan.model.domain.User;
import com.yuyan.service.UserService;
import com.yuyan.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author lucky
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2023-07-20 00:18:58
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 盐值 ==》 用来混淆密码
     */
    private static final String SALT = "yuyan";

    /**
     * 用户注册
     *
     * @param userAccount   账户
     * @param userPassword  密码
     * @param checkPassword 校验码
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        //校验数据非空
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数为空");
        }
        //账号长度不小于4位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号长度小于4位");
        }
        //密码不小于8位
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码长度小于8位");
        }
        //账户不包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号存在特殊字符");
        }
        //密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "两次密码输入不一致");
        }

        //校验星球编号的长度
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "星球编号太长");
        }

        //账户不能重复
//        QueryWrapper<User> wrapper = new QueryWrapper<>();
//        wrapper.eq("userAccount",userAccount);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserAccount, userAccount);
        long count = userMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAM_NULL, "用户已存在");
        }
        //星球编号不能重复
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPlanetCode, planetCode);
        count = userMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAM_NULL, "星球编号已存在");
        }

        //密码加密
        final String SALT = "yuyan";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        user.setCreateTime(new Date());
        int insert = userMapper.insert(user);
        if (insert < 0) {
            throw new BusinessException(ErrorCode.EXECUTE_ERR, "执行失败");
        }
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount  账户
     * @param userPassword 密码
     * @return 脱敏后的用户信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验数据非空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数为空");
        }
        //账号长度不小于4位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号长度小于4位");
        }
        //密码不小于8位
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码长度小于8位");
        }
        //账户不包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号存在特殊字符");
        }

        //密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //2.校验密码是否输入正确
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserAccount, userAccount);
        wrapper.eq(User::getUserPassword, encryptPassword);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            log.info("user login failed,UserAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAM_NULL, "用户不存在");
        }
        //3.用户信息脱敏处理
        User safeUser = getSafetyUser(user);
        //4.记录用户登录的状态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATUS, safeUser);
        //返回脱敏后的用户信息
        return safeUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL, "用户不存在");
        }
        User safeUser = new User();
        safeUser.setId(originUser.getId());
        safeUser.setUsername(originUser.getUsername());
        safeUser.setUserAccount(originUser.getUserAccount());
        safeUser.setAvatarUrl(originUser.getAvatarUrl());
        safeUser.setGender(originUser.getGender());
        safeUser.setPhone(originUser.getPhone());
        safeUser.setEmail(originUser.getEmail());
        safeUser.setUserStatus(originUser.getUserStatus());
        safeUser.setUserRole(originUser.getUserRole());
        safeUser.setPlanetCode(originUser.getPlanetCode());
        safeUser.setCreateTime(originUser.getCreateTime());
        safeUser.setTags(originUser.getTags());
        return safeUser;
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATUS);
        return -1;
    }

    /**
     * 获取当前登录用户信息
     * @param request
     * @return
     */
    @Override
    public User getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        User currentUser = (User) userObj;
        if (currentUser == null){
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        //获取用户id
        Long userId = currentUser.getId();
        User user = userMapper.selectById(userId);
        User safetyUser = getSafetyUser(user);
        return safetyUser;
    }

    /**
     * 根据标签搜索用户 (内存查询版)
     * @param tagNameList
     * @return
     */
    @Override
    public  List<User> searchUsersByTags(List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        //查询所有的用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        List<User> userList = userMapper.selectList(wrapper);
        //Gson提供的Gson对象
        Gson gson = new Gson();
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            Set<String> tempTagNameSet =  gson.fromJson(tagsStr,new TypeToken<Set<String>>(){}.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(user -> {
            log.info("user:" + user);
            return getSafetyUser(user);
        }).collect(Collectors.toList());
    }

    /**
     * 推荐页面（主页接口）
     * @param pageModel
     * @param request
     * @return
     */
    @Override
    public Page<User> recommend(Page<User> pageModel, HttpServletRequest request) {
        //先查缓存
        User currentUser = getCurrentUser(request);
        String redisKey = String.format("partner:user:recommend:%s",currentUser.getId());
        Page<User> userPage = (Page<User>) redisTemplate.opsForValue().get(redisKey);
        if (userPage != null){
            return userPage;
        }
        //如果没有缓存，再查数据库
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        userPage = userMapper.selectPage(pageModel, queryWrapper);
        //写入缓存
        try {
            redisTemplate.opsForValue().set(redisKey,userPage,60000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error:{}",e);
        }
        return userPage;
    }

    /**
     * 修改用户信息
     * @param user
     * @return
     */
    @Override
    public int updateUser(User user,HttpServletRequest request) {
        long userId = user.getId();
        if (userId < 0){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (request == null){
            return 0;
        }
        User loginUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        //如果为管理员可以更新任何用户
        //如果不是管理员,只允许更新自己的信息
        if (!isAdmin(loginUser) && loginUser.getId() != user.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null){
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }
        return userMapper.updateById(user);
    }

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        User user = (User) userObj;
        if (user != null && user.getUserRole() != UserConstant.ADMIN_ROLE) {
            return false;
        }
        return true;
    }

    /**
     * 是否为管理员
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser){
        return loginUser != null && loginUser.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    /**
     * 根据标签搜索用户 (SQL 查询版)
     *
     * @param tagNameList
     * @return
     */
    @Deprecated
    private List<User> searchUsersByTagsBySQL(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        for (String tagName : tagNameList) {
            wrapper.like(User::getTags, tagName);
        }
        List<User> userList = userMapper.selectList(wrapper);
        return userList.stream().map(user -> {
            return getSafetyUser(user);
        }).collect(Collectors.toList());
    }
}




