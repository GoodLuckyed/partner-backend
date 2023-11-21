package com.yuyan.service;


import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author yuyan
 * @date 2023/11/21
 */
@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void testRedisson(){
        RList<String> rList = redissonClient.getList("test-list");
        rList.add("yuyan");
        System.out.println(rList.get(0));
    }

    @Test
    void testWatchDog(){
        RLock lock = redissonClient.getLock("partner:precachejob:docache:lock");
        try {
            if (lock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                Thread.sleep(30000);
                System.out.println("getlock:" + Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            //释放锁,只能释放自己的锁
            if (lock.isHeldByCurrentThread()){
                System.out.println("unlock:" + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}
