package com.yuyan.service;

import com.yuyan.model.domain.PostComments;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuyan.model.request.AddCommentRequest;
import com.yuyan.model.vo.PostCommentsVo;

import java.util.List;

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

    /**
     * 根据帖文id获取评论列表
     * @param id
     * @param userId
     * @return
     */
    List<PostCommentsVo> listPostComments(Long id, Long userId);

    /**
     * 根据评论id获取评论
     * @param id
     * @return
     */
    PostCommentsVo getCommentById(Long id,Long userId);

    /**
     * 根据评论id删除评论
     * @param id
     * @param userId
     * @param isAdmin
     */
    void deleteComment(Long id, Long userId, boolean isAdmin);


    /**
     * 点赞评论
     * @param id 评论id
     * @param userId 用户id
     */
    void likeComment(Long id, Long userId);
}
