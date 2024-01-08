package com.yuyan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.yuyan.common.ErrorCode;
import com.yuyan.exception.BusinessException;
import com.yuyan.model.domain.CommentLike;
import com.yuyan.model.domain.Post;
import com.yuyan.model.domain.PostComments;
import com.yuyan.model.domain.User;
import com.yuyan.model.request.AddCommentRequest;
import com.yuyan.model.vo.PostCommentsVo;
import com.yuyan.model.vo.UserVo;
import com.yuyan.service.CommentLikeService;
import com.yuyan.service.PostCommentsService;
import com.yuyan.mapper.PostCommentsMapper;
import com.yuyan.service.PostService;
import com.yuyan.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author lucky
* @description 针对表【post_comments】的数据库操作Service实现
* @createDate 2024-01-02 23:57:44
*/
@Service
public class PostCommentsServiceImpl extends ServiceImpl<PostCommentsMapper, PostComments> implements PostCommentsService{


    @Autowired
    private PostService postService;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentLikeService commentLikeService;

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

    /**
     * 根据帖文id获取评论列表
     * @param id 帖文id
     * @param userId
     * @return
     */
    @Override
    public List<PostCommentsVo> listPostComments(Long id, Long userId) {
        LambdaQueryWrapper<PostComments> commentsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commentsLambdaQueryWrapper.eq(PostComments::getPostId,id);
        List<PostComments> postCommentsList = this.list(commentsLambdaQueryWrapper);
        return postCommentsList.stream().map((postComments) -> {
            PostCommentsVo postCommentsVo = new PostCommentsVo();
            BeanUtils.copyProperties(postComments, postCommentsVo);
            //获取评论的用户信息
            User user = userService.getById(postComments.getUserId());
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(user, userVo);
            postCommentsVo.setCommentUser(userVo);
            //判断当前用户是否点赞过评论
            LambdaQueryWrapper<CommentLike> commentLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
            commentLikeLambdaQueryWrapper.eq(CommentLike::getCommentId, postComments.getId()).eq(CommentLike::getUserId, userId);
            long count = commentLikeService.count(commentLikeLambdaQueryWrapper);
            postCommentsVo.setIsLike(count > 0);
            return postCommentsVo;
        }).collect(Collectors.toList());
    }

    /**
     * 根据评论id获取评论
     * @param id
     * @return
     */
    @Override
    public PostCommentsVo getCommentById(Long id, Long userId) {
        PostComments postComments = this.getById(id);
        if (postComments == null){
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }
        PostCommentsVo postCommentsVo = new PostCommentsVo();
        BeanUtils.copyProperties(postComments,postCommentsVo);
        LambdaQueryWrapper<CommentLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommentLike::getCommentId,id).eq(CommentLike::getUserId,userId);
        long count = commentLikeService.count(queryWrapper);
        postCommentsVo.setIsLike(count > 0);
        return postCommentsVo;
    }

    /**
     * 根据评论id删除评论
     * @param id
     * @param userId
     * @param isAdmin
     */
    @Override
    public void deleteComment(Long id, Long userId, boolean isAdmin) {
        PostComments postComments = this.getById(id);
        if (postComments == null){
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }
        if (isAdmin){
            this.removeById(id);
            Integer commentNum = postService.getById(postComments.getPostId()).getComments();
            postService.update().set("comments",commentNum - 1).eq("id",postComments.getPostId()).update();
            return;
        }
        if(!postComments.getUserId().equals(userId)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        this.removeById(id);
        Integer commentNum = postService.getById(postComments.getPostId()).getComments();
        postService.update().set("comments",commentNum - 1).eq("id",postComments.getPostId()).update();
    }
}
































