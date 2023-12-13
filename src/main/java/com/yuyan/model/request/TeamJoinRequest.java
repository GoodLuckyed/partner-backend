package com.yuyan.model.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lucky
 * @date 2023/12/13
 */
@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = 1186143591125451537L;
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long teamId;

    /**
     * 密码
     */
    private String password;
}
