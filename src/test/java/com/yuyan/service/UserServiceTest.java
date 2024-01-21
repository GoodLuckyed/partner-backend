package com.yuyan.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.yuyan.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void testAddCount() {
        User user = new User();
        user.setUsername("dogYuyan");
        user.setUserAccount("123");
        user.setAvatarUrl("https://t11.baidu.com/it/u=1683902884,1968350863&fm=58");
        user.setGender(0);
        user.setUserPassword("456");
        user.setPhone("123456");
        user.setEmail("123@qq.com");
        user.setUserStatus(0);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setIsDelete(0);
        boolean result = userService.save(user);
        Assertions.assertTrue(result);
        System.out.println(user.getId());
    }

    @Test
    void testUserRegister() {
//        String userAccount = "";
//        String userPassword = "123";
//        String checkPassword = "123";
//        String planetCode = "1";
//        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1, result);
//        userAccount = "yay";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1, result);
//        userPassword = "1234567";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1, result);
//        userAccount = "dogYuyan";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1, result);
//        userAccount = "da ydd";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1, result);
//        userAccount = "huahua";
//        userPassword = "123456789";
//        checkPassword = "1234567888";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertEquals(-1, result);
//
//        userAccount = "catyuyan";
//        userPassword = "123456789";
//        checkPassword = "123456789";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        Assertions.assertTrue(result > 0);

    }
//    @Test
//    void testUserRegister_1(){
//        String userAccount = "zhangsan";
//        String userPassword = "123456789";
//        String checkPassword = "123456789";
//        userService.userRegister(userAccount, userPassword, checkPassword,);
//    }

    @Test
    void testSearchUsersByTags() {
        List<String> tagNameList = Arrays.asList("java", "python");
        List<User> userList = userService.searchUsersByTags(tagNameList);
        Assertions.assertNotNull(userList);
    }
}
