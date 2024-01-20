package com.yuyan.model.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * @author lucky
 * @date 2024/1/20
 */

@Data
public class UserAvatarRequest implements Serializable {
    private static final long serialVersionUID = -2325519726992167048L;
    private long id;
    private MultipartFile avatarImg;
}
