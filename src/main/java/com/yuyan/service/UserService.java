package com.yuyan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuyan.model.domain.User;
import com.yuyan.model.request.UserAvatarRequest;
import com.yuyan.model.request.UserUpdatePassword;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @description 针对表【user】的数据库操作Service
* @createDate 2023-07-20 00:18:58
 * @author lucky
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount 账户
     * @param userPassword 密码
     * @param checkPassword 校验码
     * @param planetCode 星球编号
     * @return
     */
    long userRegister(String userAccount,String userPassword,String checkPassword,String planetCode);

    /**
     * 用户登录
     * @param userAccount 账户
     * @param userPassword 密码
     * @param request
     * @return 脱敏后的用户信息
     */
    String userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 获取当前登录用户信息
     * @param request
     * @return
     */
    User getCurrentUser(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     * @param tagNameList
     * @return
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * 推荐页面（主页接口）
     * @param pageModel
     * @param request
     * @return
     */
    Page<User> recommend(Page<User> pageModel, HttpServletRequest request);

    /**
     * 修改用户信息
     * @param user
     * @return
     */
    int updateUser(User user,HttpServletRequest request);

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);

    /**
     * 获取最匹配的用户
     * @param num
     * @param currentUser
     * @return
     */
    List<User> matchUsers(long num, User currentUser);

    /**
     * 修改密码
     * @param userUpdatePassword
     * @param currentUser
     * @return
     */
    int updatePassword(UserUpdatePassword userUpdatePassword, User currentUser);

    /**
     * 上传头像
     * @param userAvatarRequest
     * @param currentUser
     */
    void uploadAvatar(UserAvatarRequest userAvatarRequest, User currentUser);
}
