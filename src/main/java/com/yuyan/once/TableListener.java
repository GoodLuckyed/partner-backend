package com.yuyan.once;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TableListener implements ReadListener<XingQiuInfo> {


    /**
     * 这个每一条数据解析都会来调用
     *
     * @param xingQiuInfo    one row value. Is is same as {@link AnalysisContext#readRowHolder()}
     * @param analysisContext
     */
    @Override
    public void invoke(XingQiuInfo xingQiuInfo, AnalysisContext analysisContext) {
        System.out.println(xingQiuInfo);
    }

    /**
     * 所有数据解析完成了 都会来调用
     *
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        System.out.println("已解析完成！！！");
    }
}