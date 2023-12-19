package com.yuyan.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lucky
 * @date 2023/12/18
 */

@Data
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = -8608383594618575832L;

    /**
     * 队伍id
     */
    private Long teamId;
}
