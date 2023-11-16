package com.yuyan.job;

/**
 * @author lucky
 * @date 2023/11/16
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuyan.model.domain.User;
import com.yuyan.service.UserService;
import lombok.extern.slf4j.Slf4j;
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

    //假设重点用户
    private List<Long> mainUserList  = Arrays.asList(1L);

    @Scheduled(cron = "0 50 14 * * ? ")  //一天运行一次
    public void doCacheRecommendUser(){
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
}
