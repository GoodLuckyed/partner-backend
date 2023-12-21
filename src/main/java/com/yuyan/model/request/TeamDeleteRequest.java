package com.yuyan.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lucky
 * @date 2023/12/20
 */
@Data
public class TeamDeleteRequest implements Serializable {

    private static final long serialVersionUID = 6200185460609011349L;

    /**
     * 队伍id
     */
    private Long teamId;
}
