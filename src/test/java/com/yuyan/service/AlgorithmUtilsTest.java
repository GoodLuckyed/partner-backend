package com.yuyan.service;

/**
 * @author lucky
 * @date 2023/12/20
 */

import com.yuyan.utils.AlgorithmUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 算法工具类测试
 */

@SpringBootTest
public class AlgorithmUtilsTest {

    @Test
    void test(){
        String str1 = "我是中国人";
        String str2 = "我是一名中国人";
        String str3 = "我是中国人，我爱我的祖国";
        int score1 = AlgorithmUtils.minDistance(str1, str2);
        int score2 = AlgorithmUtils.minDistance(str1, str3);

        System.out.println("score1 = " + score1);
        System.out.println("score2 = " + score2);
    }
    @Test
    void testTags(){
        List<String> list1 = Arrays.asList("java", "大一", "男");
        List<String> list2 = Arrays.asList("java", "大二", "男");
        List<String> list3 = Arrays.asList("c++", "大一", "女");

        int score1 = AlgorithmUtils.minDistance(list1, list2);
        int score2 = AlgorithmUtils.minDistance(list1, list3);

        System.out.println("score1 = " + score1);
        System.out.println("score2 = " + score2);
    }
}
