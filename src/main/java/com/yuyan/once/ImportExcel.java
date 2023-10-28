package com.yuyan.once;

/**
 * @author yuyan
 * @date 2023/10/26
 */

import com.alibaba.excel.EasyExcel;

import java.io.File;
import java.util.List;

/**
 * 导入Excel表信息
 */
public class ImportExcel {
    public static void main(String[] args) {
        /**
         * 读取数据
         */
        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        // since: 3.0.0-beta1
//        String fileName = "E:\\星球项目\\伙伴匹配系统\\partner-backend\\src\\main\\resources\\testExcel.xlsx";
//        readListener(fileName);
        synchronousRead();
    }

    /**
     * 监听器读取
     *
     * @param fileName
     */
    public static void readListener(String fileName) {
        // 这里默认每次会读取100条数据 然后返回过来 直接调用使用数据就行
        // 具体需要返回多少行可以在`PageReadListener`的构造函数设置
        EasyExcel.read(fileName, XingQiuInfo.class, new TableListener()).sheet().doRead();
    }

    /**
     * 同步读取
     */
    public static void synchronousRead() {
        String fileName = "E:\\星球项目\\伙伴匹配系统\\partner-backend\\src\\main\\resources\\testExcel.xlsx";
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<XingQiuInfo> xingQiuInfoList = EasyExcel.read(fileName).head(XingQiuInfo.class).sheet().doReadSync();
        for (XingQiuInfo xingQiuInfo : xingQiuInfoList) {
            System.out.println(xingQiuInfo);
        }
    }
}