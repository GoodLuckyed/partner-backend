package com.yuyan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuyan.model.domain.Notice;
import com.yuyan.service.NoticeService;
import com.yuyan.mapper.NoticeMapper;
import org.springframework.stereotype.Service;

/**
* @author lucky
* @description 针对表【notice】的数据库操作Service实现
* @createDate 2023-12-26 21:27:23
*/
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice>
    implements NoticeService{

}




