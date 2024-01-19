package com.yuyan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.yuyan.common.ErrorCode;
import com.yuyan.constant.RedisConstants;
import com.yuyan.constant.SystemConstants;
import com.yuyan.exception.BusinessException;
import com.yuyan.model.domain.Follow;
import com.yuyan.model.domain.Post;
import com.yuyan.model.domain.PostLike;
import com.yuyan.model.domain.User;
import com.yuyan.model.request.PostAddRequest;
import com.yuyan.model.request.PostUpdateRequest;
import com.yuyan.model.vo.PostVo;
import com.yuyan.model.vo.UserVo;
import com.yuyan.service.*;
import com.yuyan.mapper.PostMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author lucky
 * @description 针对表【post】的数据库操作Service实现
 * @createDate 2023-12-30 01:49:24
 */
@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Autowired
    private FileUploadService fileUploadService;
    @Autowired
    private PostLikeService postLikeService;
    @Autowired
    private UserService userService;
    @Autowired
    private FollowService followService;
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 添加帖文
     *
     * @param postAddRequest
     * @param currentUser
     */
    @Override
    public boolean addPost(PostAddRequest postAddRequest, User currentUser) {
        if (StringUtils.isBlank(postAddRequest.getTitle()) || StringUtils.isBlank(postAddRequest.getContent())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "标题或内容不能为空");
        }
        Post post = new Post();
        post.setTitle(postAddRequest.getTitle());
        post.setContent(postAddRequest.getContent());
        post.setUserId(currentUser.getId());
        try {
            MultipartFile image = postAddRequest.getImage();
            String imageUrl = null;
            if (image != null) {
                imageUrl = fileUploadService.fileUpload(image);
            }
            post.setImage(imageUrl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        boolean result = this.save(post);
        //todo 推送新发布的帖文给粉丝
        return result;
    }

    /**
     * 分页查询帖文
     *
     * @param currentPage
     * @param title
     * @param userId
     * @return
     */
    @Override
    public Page<PostVo> listPostPage(long currentPage, String title, Long userId) {
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(title), Post::getTitle, title);
        queryWrapper.orderBy(true, false, Post::getCreateTime);
        Page<Post> postPage = this.page(new Page<>(currentPage, SystemConstants.PageSize), queryWrapper);
        Page<PostVo> postVoPage = new Page<>();
        BeanUtils.copyProperties(postPage, postVoPage);
        List<PostVo> postVoList = postPage.getRecords().stream().map((post) -> {
            PostVo postVo = new PostVo();
            BeanUtils.copyProperties(post, postVo);
            if (userId != null) {
                //判断当前用户是否点赞
                LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(PostLike::getPostId, post.getId()).eq(PostLike::getUserId, userId);
                long count = postLikeService.count(wrapper);
                postVo.setIsLike(count > 0);
            }
            return postVo;
        }).collect(Collectors.toList());
        postVoPage.setRecords(postVoList);
        return postVoPage;
    }

    /**
     * 根据标题查询帖文
     * @param title
     * @param userId
     * @return
     */
    @Override
    public List<PostVo> getPostByTitle(String title, Long userId) {
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(title), Post::getTitle, title);
        queryWrapper.orderBy(true, false, Post::getCreateTime);
        List<Post> postList = this.list(queryWrapper);
        List<PostVo> postVoList = postList.stream().map((post) -> {
            PostVo postVo = new PostVo();
            BeanUtils.copyProperties(post, postVo);
            if (userId != null) {
                //判断当前用户是否点赞
                LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(PostLike::getPostId, post.getId()).eq(PostLike::getUserId, userId);
                long count = postLikeService.count(wrapper);
                postVo.setIsLike(count > 0);
            }
            return postVo;
        }).collect(Collectors.toList());
        return postVoList;
    }

    /**
     * 根据id查询帖文
     *
     * @param id
     * @param userId
     * @return
     */
    @Override
    public PostVo getPostById(Long id, Long userId) {
        Post post = this.getById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL, "该帖文不存在");
        }
        PostVo postVo = new PostVo();
        BeanUtils.copyProperties(post, postVo);
        //判断当前用户是否点赞
        LambdaQueryWrapper<PostLike> likeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        likeLambdaQueryWrapper.eq(PostLike::getPostId, id).eq(PostLike::getUserId, userId);
        long count = postLikeService.count(likeLambdaQueryWrapper);
        postVo.setIsLike(count > 0);

        //查询帖文作者信息
        User author = userService.getById(post.getUserId());
        if (author == null) {
            throw new BusinessException(ErrorCode.PARAM_NULL, "该作者不存在");
        }
        UserVo authorVO = new UserVo();
        BeanUtils.copyProperties(author, authorVO);
        //判断当前用户是否关注作者
        LambdaQueryWrapper<Follow> followLambdaQueryWrapper = new LambdaQueryWrapper<>();
        followLambdaQueryWrapper.eq(Follow::getUserId, userId).eq(Follow::getFollowUserId, author.getId());
        long count1 = followService.count(followLambdaQueryWrapper);
        authorVO.setIsFollow(count1 > 0);
        postVo.setAuthor(authorVO);
        return postVo;
    }

    /**
     * 根据id删除帖文
     *
     * @param id
     * @param userId
     * @param isAdmin
     * @return
     */
    @Override
    public Boolean deletePostById(Long id, Long userId, boolean isAdmin) {
        //管理员可以删除
        if (isAdmin) {
            return this.removeById(id);
        }
        //判断当前用户是否是帖文作者
        Post post = this.getById(id);
        if (!userId.equals(post.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "您不是该帖文的作者，无法删除");
        }
        this.removeById(id);
        return true;
    }

    /**
     * 更新帖文
     *
     * @param postUpdateRequest
     * @param userId
     * @param isAdmin
     * @return
     */
    @Override
    public Boolean updatePost(PostUpdateRequest postUpdateRequest, Long userId, boolean isAdmin) {
        Long id = postUpdateRequest.getId();
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该帖文不存在");
        }
        Post post = this.getById(id);
        //只有管理员和帖文作者可以更新帖文
        if (!userId.equals(post.getUserId()) && !isAdmin) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        Post newPost = new Post();
        newPost.setId(id);
        if (StringUtils.isAnyBlank(postUpdateRequest.getTitle(), postUpdateRequest.getContent())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "标题或内容不能为空");
        }
        newPost.setTitle(postUpdateRequest.getTitle());
        newPost.setContent(postUpdateRequest.getContent());
        MultipartFile image = postUpdateRequest.getImage();
        try {
            String imageUrl = null;
            if (image != null) {
                imageUrl = fileUploadService.fileUpload(image);
            }
            newPost.setImage(imageUrl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.updateById(newPost);
        return true;
    }

    /**
     * 点赞帖文
     *
     * @param id
     * @param userId
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void likePost(Long id, Long userId) {
        RLock lock = redissonClient.getLock(RedisConstants.POST_LIKE_KEY + id + ":" + userId);
        try {
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                Post post = this.getById(id);
                if (post == null) {
                    throw new BusinessException(ErrorCode.PARAM_NULL, "该帖文不存在");
                }
                LambdaQueryWrapper<PostLike> likeLambdaQueryWrapper = new LambdaQueryWrapper<>();
                likeLambdaQueryWrapper.eq(PostLike::getPostId, id).eq(PostLike::getUserId, userId);
                long count = postLikeService.count(likeLambdaQueryWrapper);
                if (count > 0) {
                    //已经点赞过，取消点赞
                    postLikeService.remove(likeLambdaQueryWrapper);
                    Integer likeNum = post.getLikes();
                    //更新数据库
                    this.update().eq("id", id).set("likes", likeNum - 1).update();
                    //todo 消息通知
                } else {
                    //未点赞，可以点赞
                    PostLike postLike = new PostLike();
                    postLike.setPostId(id);
                    postLike.setUserId(userId);
                    postLikeService.save(postLike);
                    Integer likeNum = post.getLikes();
                    //更新数据库
                    this.update().eq("id", id).set("likes", likeNum + 1).update();
                    //todo 消息通知
                }
            }
        } catch (InterruptedException e) {
            log.error("postLike error", e);
        } finally {
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }

    /**
     * 获取我写的帖文
     * @param currentUser
     * @return
     */
    @Override
    public List<PostVo> myPost(User currentUser) {
        Long userId = currentUser.getId();
        LambdaQueryWrapper<Post> postLambdaQueryWrapper = new LambdaQueryWrapper<>();
        postLambdaQueryWrapper.eq(Post::getUserId,userId);
        List<Post> postList = this.list(postLambdaQueryWrapper);
        List<PostVo> postVoList = postList.stream().map(post -> {
            PostVo postVo = new PostVo();
            BeanUtils.copyProperties(post, postVo);
            User user = userService.getById(userId);
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(user, userVo);
            postVo.setAuthor(userVo);
            LambdaQueryWrapper<PostLike> likeLambdaQueryWrapper = new LambdaQueryWrapper<>();
            likeLambdaQueryWrapper.eq(PostLike::getUserId,userId).eq(PostLike::getPostId,post.getId());
            long count = postLikeService.count(likeLambdaQueryWrapper);
            postVo.setIsLike(count > 0);
            return postVo;
        }).collect(Collectors.toList());
        return postVoList;
    }
}












