package com.yuyan.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lucky
 * @date 2024/1/20
 */
@Data
public class UserUpdatePassword implements Serializable {
    private static final long serialVersionUID = 441618999552851617L;
    long id;
    private String oldPassword;
    private String newPassword;
    private String checkPassword;
}



