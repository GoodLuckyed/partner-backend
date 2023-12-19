package com.yuyan.model.request;

import lombok.Data;
import java.io.Serializable;


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
    private Long teamId;

    /**
     * 密码
     */
    private String password;
}
