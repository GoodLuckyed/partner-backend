package com.yuyan.job;

/**
 * @author yuyan
 * @date 2023/11/16
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuyan.model.domain.User;
import com.yuyan.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    //假设重点用户
    private List<Long> mainUserList  = Arrays.asList(1L);

    @Scheduled(cron = "0 41 20 * * ? ")  //一天运行一次
    public void doCacheRecommendUser(){
        RLock lock = redissonClient.getLock("partner:precachejob:docache:lock");
        try {
            if (lock.tryLock(0,30000L,TimeUnit.MILLISECONDS)){
                System.out.println("getlock:" + Thread.currentThread().getId());
                for (Long userId : mainUserList) {
                    LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 4), queryWrapper);
                    String redisKey = String.format("partner:user:recommend:%s",userId);
                    ValueOperations valueOperations = redisTemplate.opsForValue();

                    try {
                        valueOperations.set(redisKey,userPage,60000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error:{}",e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error",e);
        } finally {
            //释放锁,只能释放自己的锁
            if (lock.isHeldByCurrentThread()){
                System.out.println("unlock:" + Thread.currentThread().getId());
                lock.unlock();
            }
        }

    }
}
