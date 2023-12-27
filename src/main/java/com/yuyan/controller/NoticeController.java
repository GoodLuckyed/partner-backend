package com.yuyan.controller;

import com.yuyan.common.BaseResponse;
import com.yuyan.common.ErrorCode;
import com.yuyan.common.ResultUtils;
import com.yuyan.exception.BusinessException;
import com.yuyan.model.domain.Notice;
import com.yuyan.model.domain.User;
import com.yuyan.model.request.NoticeAddRequest;
import com.yuyan.model.request.NoticeUpdateRequest;
import com.yuyan.service.NoticeService;
import com.yuyan.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author lucky
 * @date 2023/12/26
 */

@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;
    @Autowired
    private UserService userService;

    @ApiOperation(value = "添加公告")
    @PostMapping("/add")
    public BaseResponse<Long> addNotice(@RequestBody NoticeAddRequest noticeAddRequest, HttpServletRequest request) {
        //1.判断请求参数是否为空
        if (noticeAddRequest == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        //2.判断当前登录用户是否为管理员
        boolean admin = userService.isAdmin(request);
        if (!admin){
            throw new BusinessException(ErrorCode.NO_AUTH,"当前用户不是管理员");
        }
        Notice notice = new Notice();
        BeanUtils.copyProperties(noticeAddRequest,notice);
        long userId = currentUser.getId();
        notice.setUserId(userId);
        //3.添加公告
        boolean result = noticeService.save(notice);
        if (!result){
            throw new BusinessException(ErrorCode.EXECUTE_ERR,"添加公告失败");
        }
        return ResultUtils.success(notice.getId());
    }

    @ApiOperation(value = "根据id获取公告")
    @GetMapping("/get")
    public BaseResponse<Notice> getNoticeById(@RequestParam(value = "noticeId") Long noticeId){
        if (noticeId < 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Notice notice = noticeService.getById(noticeId);
        if (notice == null){
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }
        return ResultUtils.success(notice);
    }

    @ApiOperation(value = "获取公告列表")
    @GetMapping("/list")
    public BaseResponse<List<Notice>> listNotice(){
        List<Notice> noticeList = noticeService.list();
        //按照创建时间和更新时间降序排序
        List<Notice> sortNoticeList = noticeList.stream().sorted(
                        Comparator.comparing(Notice::getUpdateTime)
                                .thenComparing(Notice::getCreateTime).reversed())
                .collect(Collectors.toList());
        return ResultUtils.success(sortNoticeList);
    }

    @ApiOperation(value = "更新公告")
    @PostMapping("/update")
    public BaseResponse<Boolean> updateNotice(@RequestBody NoticeUpdateRequest noticeUpdateRequest,HttpServletRequest request){
        //1.判断请求参数是否为空
        if (noticeUpdateRequest == null || noticeUpdateRequest.getId() < 0){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        //2.判断当前登录用户是否为管理员
        boolean admin = userService.isAdmin(request);
        if (!admin){
            throw new BusinessException(ErrorCode.NO_AUTH,"当前用户不是管理员");
        }
        Long noticeId = noticeUpdateRequest.getId();
        Notice oldNotice = noticeService.getById(noticeId);
        //3.判断公告是否存在
        if (oldNotice == null){
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }
        Notice notice = new Notice();
        BeanUtils.copyProperties(noticeUpdateRequest,notice);
        boolean result = noticeService.updateById(notice);
        return ResultUtils.success(result);
    }

    @ApiOperation(value = "根据id删除公告")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteNoticeById(@RequestParam(value = "noticeId") Long noticeId,HttpServletRequest request){
        if (noticeId < 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        //1.判断当前登录用户是否为管理员
        boolean admin = userService.isAdmin(request);
        if (!admin){
            throw new BusinessException(ErrorCode.NO_AUTH,"当前用户不是管理员");
        }
        //2.判断公告是否存在
        Notice notice = noticeService.getById(noticeId);
        if (notice == null){
            throw new BusinessException(ErrorCode.PARAM_NULL);
        }
        //3.删除公告
        boolean result = noticeService.removeById(noticeId);
        return ResultUtils.success(result);
    }
}
















