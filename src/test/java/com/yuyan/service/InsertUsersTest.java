package com.yuyan.service;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import com.yuyan.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

/**
 * @author yuyan
 * @date 2023/11/9
 */

@SpringBootTest
public class InsertUsersTest {

    @Autowired
    private UserService userService;

    private ExecutorService executorService = new ThreadPoolExecutor(16, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    /**
     * 循环插入用户 -> 批量插入用户
     */
    @Test
    public void doInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
        ArrayList<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("测试用户");
            user.setUserAccount("testUser");
            user.setAvatarUrl("http://t15.baidu.com/it/u=1155900523,2163568117&fm=224&app=112&f=JPEG?w=500&h=500");
            user.setGender(0);
            user.setUserPassword("123456789");
            user.setPhone("123456");
            user.setEmail("123@qq.com");
            user.setUserStatus(0);
            user.setPlanetCode("100");
            user.setUserRole(0);
            user.setTags("[]");
            userList.add(user);
        }
        userService.saveBatch(userList,100);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    /**
     * 并发批量插入
     */
    @Test
    public void doConcurrencyInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        // 分十组
        int j = 0;
        //批量插入数据的大小
        int batchSize = 5000;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM / batchSize; i++) {
            List<User> userList = new ArrayList<>();
            while (true) {
                j++;
                User user = new User();
                user.setUsername("假shier");
                user.setUserAccount("shier");
                user.setAvatarUrl("https://c-ssl.dtstatic.com/uploads/blog/202101/11/20210111220519_7da89.thumb.1000_0.jpeg");
                user.setGender(1);
                user.setUserPassword("12345678");
                user.setPhone("123456789108");
                user.setEmail("22288999@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setPlanetCode("33322");
                user.setTags("[]");
                userList.add(user);
                if (j % batchSize == 0) {
                    break;
                }
            }
            //异步执行 使用CompletableFuture开启异步任务
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("ThreadName：" + Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);
            }, executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();

        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());
    }
}

