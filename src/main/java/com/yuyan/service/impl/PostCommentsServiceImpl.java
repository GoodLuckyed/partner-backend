package com.yuyan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuyan.model.domain.Post;
import com.yuyan.model.domain.PostComments;
import com.yuyan.model.request.AddCommentRequest;
import com.yuyan.service.PostCommentsService;
import com.yuyan.mapper.PostCommentsMapper;
import com.yuyan.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author lucky
* @description 针对表【post_comments】的数据库操作Service实现
* @createDate 2024-01-02 23:57:44
*/
@Service
public class PostCommentsServiceImpl extends ServiceImpl<PostCommentsMapper, PostComments> implements PostCommentsService{


    @Autowired
    private PostService postService;

    /**
     * 添加评论
     * @param addCommentRequest
     * @param userId
     */
    @Override
    public void addComment(AddCommentRequest addCommentRequest, long userId) {
        PostComments postComments = new PostComments();
        postComments.setPostId(addCommentRequest.getPostId());
        postComments.setContent(addCommentRequest.getContent());
        postComments.setUserId(userId);
        postComments.setLikes(0);
        postComments.setStatus(0);
        this.save(postComments);
        Post post = postService.getById(addCommentRequest.getPostId());
        postService.update().set("comments",post.getComments() + 1).eq("id",addCommentRequest.getPostId()).update();
    }
}



