package com.yuyan.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册DTO
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -6015148387998713009L;

    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private String planetCode;

}
