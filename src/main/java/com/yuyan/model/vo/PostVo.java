package com.yuyan.model.vo;

import com.yuyan.model.domain.Post;
import lombok.Data;
import java.io.Serializable;

/**
 * @author lucky
 * @date 2024/1/1
 */
@Data
public class PostVo extends Post implements Serializable {
    private static final long serialVersionUID = 3247908492947950390L;

    /**
     * 是否点赞
     */
    private Boolean isLike;

    /**
     * 帖文作者
     */
    private UserVo author;
}
