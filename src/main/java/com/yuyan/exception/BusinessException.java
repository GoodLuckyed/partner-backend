package com.yuyan.exception;

import com.yuyan.common.ErrorCode;
import lombok.Data;

/**
 * 自定义异常
 * @author yuyan
 * @date 2023/7/27 15:35
 */
@Data
public class BusinessException extends RuntimeException{


    private final int code;

    private final String description;


    public BusinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode,String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }
}
