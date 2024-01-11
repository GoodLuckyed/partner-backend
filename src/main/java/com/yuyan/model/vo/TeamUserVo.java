package com.yuyan.model.vo;

/**
 * @author lucky
 * @date 2023/12/12
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 队伍和用户信息封装类（脱敏）
 */
@Data
public class TeamUserVo implements Serializable {
    private static final long serialVersionUID = 85896544122743889L;
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
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
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id（队长 id）
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    /**
     * 创建人的用户信息
     */
    private UserVo createUser;

    /**
     * 已加入队伍的人数
     */
    private Integer hasJoinNum;

    /**
     * 是否已经加入队伍
     */
    private Boolean hasJoin = false;

    /**
     * 队伍成员列表
     */
    private List<UserVo> memberList;
}
