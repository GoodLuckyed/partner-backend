package com.yuyan.model.dto;

import com.yuyan.common.PageRequest;
import lombok.Data;

/**
 * @author lucky
 * @date 2023/12/1
 */
@Data
public class TeamQuery extends PageRequest {
    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 搜索关键词（同时对队伍名称和描述进行搜索）
     */
    private String searchText;
}
