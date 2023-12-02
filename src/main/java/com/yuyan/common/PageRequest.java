package com.yuyan.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lucky
 * @date 2023/12/1
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 2126598624546210966L;
    /**
     * 每页条数
     */
    protected int pageSize;

    /**
     * 当前页
     */
    protected int pageNum;


}
