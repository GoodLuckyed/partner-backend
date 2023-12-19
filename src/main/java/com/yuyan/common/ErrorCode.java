package com.yuyan.common;

/**
 * 错误码
 * @author lucky
 * @date 2023/7/27 10:55
 */
public enum ErrorCode {
    SUCCESS(0,"成功",""),
    PARAM_ERROR(40000,"请求参数错误",""),
    PARAM_NULL(40001,"请求数据为空",""),
    NO_LOGIN(40100,"未登录",""),
    NO_AUTH(40101,"无权限",""),
    FORBIDDEN(40300,"禁止访问",""),
    SYSTEM_ERR(50000,"系统内部错误",""),
    EXECUTE_ERR(40102,"执行错误","");


    private final int code;
    private final String message;
    private final String description;


    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
