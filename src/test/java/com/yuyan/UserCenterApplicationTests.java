package com.yuyan;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

@SpringBootTest
class UserCenterApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testAccount(){
        String userPassword = "123456";
        final String SALT = "yuyan";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        System.out.println(encryptPassword);
    }
}
