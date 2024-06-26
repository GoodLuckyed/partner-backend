package com.yuyan.service.impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.BiIntFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yuyan.common.ErrorCode;
import com.yuyan.common.ResultUtils;
import com.yuyan.constant.UserConstant;
import com.yuyan.exception.BusinessException;
import com.yuyan.model.domain.User;
import com.yuyan.model.request.UpdateTagRequest;
import com.yuyan.model.request.UserAvatarRequest;
import com.yuyan.model.request.UserUpdatePassword;
import com.yuyan.service.FileUploadService;
import com.yuyan.service.UserService;
import com.yuyan.mapper.UserMapper;
import com.yuyan.utils.AlgorithmUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private FileUploadService fileUploadService;

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
    public long userRegister(String username,String userAccount, String userPassword, String checkPassword) {
        //校验数据非空
        if (StringUtils.isAnyBlank(username,userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数为空");
        }
        if (username.length() > 20){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"用户名太长");
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
//        if (planetCode.length() > 5) {
//            throw new BusinessException(ErrorCode.PARAM_ERROR, "星球编号太长");
//        }

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
//        wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(User::getPlanetCode, planetCode);
//        count = userMapper.selectCount(wrapper);
//        if (count > 0) {
//            throw new BusinessException(ErrorCode.PARAM_NULL, "星球编号已存在");
//        }

        //密码加密
        final String SALT = "yuyan";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //插入数据
        User user = new User();
        user.setUsername(username);
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
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
    public String userLogin(String userAccount, String userPassword, HttpServletRequest request) {
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
        String token = UUID.randomUUID().toString(true);
        //4.记录用户登录的状态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATUS +token, safeUser);
        //返回脱敏后的用户信息
        return token;
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
        safeUser.setProfile(originUser.getProfile());
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
        String token = request.getHeader("Authorization");
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATUS + token);
        return -1;
    }

    /**
     * 获取当前登录用户信息
     * @param request
     * @return
     */
    @Override
    public User getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS + token);
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
                //转换为小写后进行比较
                if (!tempTagNameSet.stream().map(String::toLowerCase).collect(Collectors.toSet()).contains(tagName.toLowerCase())) {
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
//        //先查缓存
//        User currentUser = getCurrentUser(request);
//        String redisKey = String.format("partner:user:recommend:%s",currentUser.getId());
//        Page<User> userPage = (Page<User>) redisTemplate.opsForValue().get(redisKey);
//        if (userPage != null){
//            return userPage;
//        }
//        //如果没有缓存，再查数据库
//        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
//        userPage = userMapper.selectPage(pageModel, queryWrapper);
//        //写入缓存
//        try {
//            redisTemplate.opsForValue().set(redisKey,userPage,24, TimeUnit.HOURS);
//        } catch (Exception e) {
//            log.error("redis set key error:{}",e);
//        }
//        return userPage;
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        return userMapper.selectPage(pageModel, queryWrapper);
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
        String token = request.getHeader("Authorization");
        User loginUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS + token);
        //如果为管理员可以更新任何用户
        //如果不是管理员,只允许更新自己的信息
         if (!isAdmin(loginUser) && !loginUser.getId().equals(userId)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null){
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }
        return userMapper.updateById(user);
    }

    /**
     * 修改密码
     * @param userUpdatePassword
     * @param currentUser
     * @return
     */
    @Override
    public int updatePassword(UserUpdatePassword userUpdatePassword, User currentUser) {
        long id = userUpdatePassword.getId();
        String oldPassword = userUpdatePassword.getOldPassword();
        String newPassword = userUpdatePassword.getNewPassword();
        String checkPassword = userUpdatePassword.getCheckPassword();
        if (StringUtils.isAnyBlank(oldPassword,newPassword,checkPassword)){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"输入有误");
        }
        //密码不小于8位
        if (oldPassword.length() < 8 || newPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码长度小于8位");
        }
        //密码和校验密码相同
        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "两次密码输入不一致");
        }
        //权限校验
        if (!isAdmin(currentUser) && currentUser.getId() != id){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + oldPassword).getBytes());
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getUserAccount,currentUser.getUserAccount()).eq(User::getUserPassword,encryptPassword);
        User user = this.getOne(userLambdaQueryWrapper);
        if (user == null){
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }
        String newEncryptPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        user.setUserPassword(newEncryptPassword);
        return userMapper.updateById(user);
    }

    /**
     * 上传头像
     * @param userAvatarRequest
     * @param currentUser
     */
    @Override
    public void uploadAvatar(UserAvatarRequest userAvatarRequest, User currentUser) {
        long id = userAvatarRequest.getId();
        Long userId = currentUser.getId();
        if (!isAdmin(currentUser) && userId != id){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User user = this.getById(id);
        if (user == null){
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }
        try {
            MultipartFile avatarImg = userAvatarRequest.getAvatarImg();
            String avatarUrl = user.getAvatarUrl();
            if (avatarImg != null){
                avatarUrl = fileUploadService.fileUpload(avatarImg);
            }
            user.setAvatarUrl(avatarUrl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.updateById(user);
    }

    /**
     * 修改标签
     * @param updateTagRequest
     * @param currentUser
     * @return
     */
    @Override
    public int updateTag(UpdateTagRequest updateTagRequest, User currentUser) {
        long id = updateTagRequest.getId();
        if (id < 0){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        //限制标签的数量
        Set<String> newTagList = updateTagRequest.getTagList();
        if (newTagList.size() > 7){
            throw new BusinessException(ErrorCode.PARAM_ERROR,"标签数量不能超过7个");
        }
        //权限校验
        if (!isAdmin(currentUser) && currentUser.getId() != id){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User user = userMapper.selectById(id);
        Gson gson = new Gson();
        Set<String> oldTagList = gson.fromJson(user.getTags(), new TypeToken<Set<String>>() {
        }.getType());
        if (oldTagList == null){
            String newJson = gson.toJson(newTagList);
            user.setTags(newJson);
            return userMapper.updateById(user);
        }

        //转换成小写
        Set<String> oldTagListLow = oldTagList.stream().map(String::toLowerCase).collect(Collectors.toSet());
        Set<String> newTagListLow = newTagList.stream().map(String::toLowerCase).collect(Collectors.toSet());

        //添加newTagList中有,oldTagList中没有的标签
        oldTagListLow.addAll(newTagListLow.stream().filter(tag -> !oldTagListLow.contains(tag)).collect(Collectors.toSet()));
        //移除oldTagList中有,newTagList中没有的标签
        oldTagListLow.removeAll(oldTagListLow.stream().filter(tag -> !newTagListLow.contains(tag)).collect(Collectors.toSet()));
        String tagJson = gson.toJson(oldTagListLow);
        user.setTags(tagJson);
        return userMapper.updateById(user);
    }

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS + token);
        User user = (User) userObj;
        if (user == null || user.getUserRole() != UserConstant.ADMIN_ROLE) {
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
     * 获取最匹配的用户
     * @param num
     * @param currentUser
     * @return
     */
    @Override
    public List<User> matchUsers(long num, User currentUser) {
        //只查询需要的数据
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id","tags");
        queryWrapper.isNotNull("tags");
        //根据条件获取用户列表
        List<User> userList = this.list(queryWrapper);
        //获取当前用户的标签
        String tags = currentUser.getTags();
        //Gson提供的Gson对象
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        List<Pair<User,Long>> list = new ArrayList<>();
        //依次计算当前用户和所有用户的相似度
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            //过滤掉没有标签的用户,同时过滤掉自己 (可以提升查询性能)
            if (StringUtils.isBlank(userTags) || user.getId() == currentUser.getId()){
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            //计算相似度
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user,distance));
        }
        //按照相似度由小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        //原本顺序的userId列表
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        //根据userId列表查询用户信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id",userIdList);
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper).stream().map(this::getSafetyUser).collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
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




