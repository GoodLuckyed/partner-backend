package com.yuyan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuyan.model.domain.Post;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuyan.model.domain.User;
import com.yuyan.model.request.PostAddRequest;
import com.yuyan.model.request.PostUpdateRequest;
import com.yuyan.model.vo.PostVo;

import java.util.List;

/**
* @author lucky
* @description 针对表【post】的数据库操作Service
* @createDate 2023-12-30 01:49:24
*/
public interface PostService extends IService<Post> {

    /**
     * 添加帖文
     * @param postAddRequest
     * @param currentUser
     */
    boolean addPost(PostAddRequest postAddRequest, User currentUser);


    /**
     * 分页查询帖文
     * @param currentPage
     * @param title
     * @param userId
     * @return
     */
    Page<PostVo> listPostPage(long currentPage, String title, Long userId);

    /**
     * 根据标题查询帖文
     * @param title
     * @param userId
     * @return
     */
    List<PostVo> getPostByTitle(String title, Long userId);

    /**
     * 根据id查询帖文
     * @param id
     * @param userId
     * @return
     */
    PostVo getPostById(Long id, Long userId);

    /**
     * 根据id删除帖文
     * @param id
     * @param userId
     * @param isAdmin
     * @return
     */
    Boolean deletePostById(Long id, Long userId, boolean isAdmin);

    /**
     * 更新帖文
     * @param postUpdateRequest
     * @param userId
     * @param isAdmin
     * @return
     */
    Boolean updatePost(PostUpdateRequest postUpdateRequest, Long userId, boolean isAdmin);

    /**
     * 点赞帖文
     * @param id
     * @param userId
     */
    void likePost(Long id, Long userId);

    /**
     * 获取我写的帖文
     * @param currentUser
     * @return
     */
    List<PostVo> myPost(User currentUser);
}
