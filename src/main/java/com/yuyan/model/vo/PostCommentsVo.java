package com.yuyan.model.vo;

import com.yuyan.model.domain.PostComments;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lucky
 * @date 2024/1/6
 */
@Data
public class PostCommentsVo extends PostComments implements Serializable {
    private static final long serialVersionUID = -4281482150441227073L;

    /**
     * 评论的用户
     */
    private UserVo commentUser;

    /**
     * 是否点赞
     */
    private Boolean isLike;
}
