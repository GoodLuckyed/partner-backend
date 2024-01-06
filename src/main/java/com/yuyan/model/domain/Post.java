package com.yuyan.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName post
 */
@TableName(value ="post")
@Data
public class Post implements Serializable {
    /**
     * 帖文id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 帖文标题
     */
    private String title;

    /**
     * 帖文内容
     */
    private String content;

    /**
     * 图片
     */
    private String image;

    /**
     * 创建人
     */
    private Long userId;

    /**
     * 点赞数量
     */
    private Integer likes;

    /**
     * 评论数量
     */
    private Integer comments;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}