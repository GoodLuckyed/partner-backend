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