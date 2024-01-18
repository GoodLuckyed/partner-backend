-- auto-generated definition

create database if not exists partner;
use partner;
create table user
(
    id           bigint auto_increment comment 'id'
        primary key,
    username     varchar(256)      null comment '用户昵称',
    userAccount  varchar(256)      null comment '账号',
    avatarUrl    varchar(1024)     null comment '用户头像',
    gender       tinyint           null comment '性别',
    userPassword varchar(512)      not null comment '密码',
    phone        varchar(128)      null comment '电话',
    email        varchar(512)      null comment '邮箱',
    userStatus   int               null comment '用户状态 0 - 正常',
    createTime   datetime          null comment '创建时间',
    updateTime   datetime          null comment '更新时间',
    userRole     int     default 0 not null comment '用户角色 0 - 普通用户 1 - 管理员',
    isDelete     tinyint default 0 not null comment '是否删除',
    planetCode   varchar(512)      null comment '星球编号',
    tags         varchar(1024)     null comment '标签列表',
    profile      varchar(512)      null comment '个人描述'
) DEFAULT CHARSET = utf8 comment '用户表';

INSERT INTO partner.user (id, username, userAccount, avatarUrl, gender, userPassword, phone, email, userStatus, createTime, updateTime, userRole, isDelete, planetCode) VALUES (1, 'dogYuyan', 'dogYuyan', 'http://t15.baidu.com/it/u=1155900523,2163568117&fm=224&app=112&f=JPEG?w=500&h=500', 0, '588ebea156ec6bbcca631fd3a1be7c62', '19876578989', '123@qq.com', 0, '2023-07-20 00:35:56', '2023-07-20 00:35:56', 0, 0, '1');
INSERT INTO partner.user (id, username, userAccount, avatarUrl, gender, userPassword, phone, email, userStatus, createTime, updateTime, userRole, isDelete, planetCode) VALUES (6, '张三', 'zhangsan', 'http://t15.baidu.com/it/u=1155900523,2163568117&fm=224&app=112&f=JPEG?w=500&h=500', null, '588ebea156ec6bbcca631fd3a1be7c62', '18746587309', '123@qq.com', null, null, null, 1, 0, '2');
INSERT INTO partner.user (id, username, userAccount, avatarUrl, gender, userPassword, phone, email, userStatus, createTime, updateTime, userRole, isDelete, planetCode) VALUES (7, 'dogyupi', 'dogyupi', 'http://t15.baidu.com/it/u=1155900523,2163568117&fm=224&app=112&f=JPEG?w=500&h=500', null, '588ebea156ec6bbcca631fd3a1be7c62', '18979056543', '123@qq.com', null, null, null, 1, 0, '3');
INSERT INTO partner.user (id, username, userAccount, avatarUrl, gender, userPassword, phone, email, userStatus, createTime, updateTime, userRole, isDelete, planetCode) VALUES (8, '小猫', 'catyupi', 'http://t15.baidu.com/it/u=1155900523,2163568117&fm=224&app=112&f=JPEG?w=500&h=500', null, '588ebea156ec6bbcca631fd3a1be7c62', '13787656789', '123@qq.com', null, null, null, 0, 0, '4');
INSERT INTO partner.user (id, username, userAccount, avatarUrl, gender, userPassword, phone, email, userStatus, createTime, updateTime, userRole, isDelete, planetCode) VALUES (9, 'yupiAdmin', 'yupiAdmin', 'http://t15.baidu.com/it/u=1155900523,2163568117&fm=224&app=112&f=JPEG?w=500&h=500', null, '588ebea156ec6bbcca631fd3a1be7c62', '16789854634', '123@qq.com', null, null, null, 1, 0, '5');
INSERT INTO partner.user (id, username, userAccount, avatarUrl, gender, userPassword, phone, email, userStatus, createTime, updateTime, userRole, isDelete, planetCode) VALUES (12, '佳妹妹', 'yuyanjia', 'http://t15.baidu.com/it/u=1155900523,2163568117&fm=224&app=112&f=JPEG?w=500&h=500', null, '588ebea156ec6bbcca631fd3a1be7c62', '17845905637', '123@qq.com', null, '2023-07-27 00:06:57', null, 0, 0, '6');



create table team
(
    id          bigint auto_increment comment 'id' primary key,
    name        varchar(256)                       not null comment '队伍名称',
    description varchar(1024)                      null comment '描述',
    maxNum      int      default 1                 not null comment '最大人数',
    expireTime  datetime                           null comment '过期时间',
    userId      bigint comment '用户id（队长 id）',
    status      int      default 0                 not null comment '0 - 公开，1 - 私有，2 - 加密',
    password    varchar(512)                       null comment '密码',
    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete    tinyint  default 0                 not null comment '是否删除'
) DEFAULT CHARSET = utf8 comment '队伍表';


create table user_team
(
    id         bigint auto_increment comment 'id'
        primary key,
    userId     bigint comment '用户id',
    teamId     bigint comment '队伍id',
    joinTime   datetime                           null comment '加入时间',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete   tinyint  default 0                 not null comment '是否删除'
) comment '用户-队伍关系';


-- (可以不用创建，标签字段放在用户表里)
create table tag
(
    id         bigint auto_increment comment 'id' primary key,
    tagName    varchar(256)                       null comment '标签名称',
    userId     bigint                             null comment '用户id',
    parentId   bigint                             null comment '父标签id',
    isParent   tinyint                            null comment '0-不是父标签, 1-是父标签',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
) comment '标签表';

-- 公告表
CREATE TABLE `notice` (
    id INT NOT NULL AUTO_INCREMENT COMMENT 'id',
    title varchar(255) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容',
    userId BIGINT NOT NULL COMMENT '创建用户id(管理员)',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    isDelete TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    CONSTRAINT notice_pk PRIMARY KEY (id)
)
    ENGINE=InnoDB
DEFAULT CHARSET=utf8
COLLATE=utf8_general_ci;


-- 帖文表
CREATE TABLE `post` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '帖文id',
    `title` varchar(255) NOT NULL COMMENT '帖文标题',
    `content` text NOT NULL COMMENT '帖文内容',
    `image` varchar(1024) DEFAULT NULL COMMENT '图片',
    `userId` bigint(20) NOT NULL COMMENT '创建人',
    `likes` int(8) DEFAULT '0' COMMENT '点赞数量',
    `comments` int(8) DEFAULT NULL COMMENT '评论数量',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` int(11) NOT NULL DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- 帖文评论表
CREATE TABLE `post_comments` (
     `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
     `userId` bigint(20) unsigned NOT NULL COMMENT '用户id',
     `postId` bigint(20) unsigned NOT NULL COMMENT '帖文id',
     `parentId` bigint(20) unsigned DEFAULT NULL COMMENT '关联的1级评论id，如果是一级评论，则值为0',
     `answerId` bigint(20) unsigned DEFAULT NULL COMMENT '回复的评论id',
     `content` varchar(255) NOT NULL COMMENT '回复的内容',
     `likes` int(8) unsigned DEFAULT NULL COMMENT '点赞数',
     `status` tinyint(1) unsigned DEFAULT NULL COMMENT '状态，0：正常，1：被举报，2：禁止查看',
     `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `updateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
     PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT;

-- 帖文点赞表
CREATE TABLE `post_like` (
     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
     `postId` bigint(20) NOT NULL COMMENT '帖文id',
     `userId` bigint(20) NOT NULL COMMENT '用户id',
     `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 评论点赞表
CREATE TABLE `comment_like` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `commentId` bigint(20) NOT NULL COMMENT '评论id',
    `userId` bigint(20) NOT NULL COMMENT '用户id',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 关注表
CREATE TABLE `follow` (
      `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
      `userId` bigint(20) NOT NULL COMMENT '用户id',
      `followUserId` bigint(20) NOT NULL COMMENT '关注的用户id',
      `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
      `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
      `isDelete` int(11) NOT NULL DEFAULT '0' COMMENT '是否删除',
      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 聊天消息表
CREATE TABLE `chat` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '聊天记录id',
    `fromId` bigint(20) NOT NULL COMMENT '发送消息用户id',
    `toId` bigint(20) DEFAULT NULL COMMENT '接受消息用户id',
    `text` varchar(512) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '消息内容',
    `chatType` tinyint(4) NOT NULL COMMENT '聊天类型 1-私聊 2-群聊',
    `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `teamId` bigint(20) DEFAULT NULL COMMENT '队伍id',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;