package com.yuyan.service;

import com.yuyan.model.domain.PostComments;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuyan.model.request.AddCommentRequest;

/**
* @author lucky
* @description 针对表【post_comments】的数据库操作Service
* @createDate 2024-01-02 23:57:44
*/
public interface PostCommentsService extends IService<PostComments> {

    /**
     * 添加评论
     * @param addCommentRequest
     * @param userId
     */
    void addComment(AddCommentRequest addCommentRequest, long userId);
}
