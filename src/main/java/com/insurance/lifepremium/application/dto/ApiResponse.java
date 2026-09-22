package com.insurance.lifepremium.application.dto;

public class ApiResponse<T> {
    private boolean success;
    private String errorCode;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.data = data;
        return r;
    }

    public static <T> ApiResponse<T> error(String errorCode, String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.errorCode = errorCode;
        r.message = message;
        return r;
    }

    public boolean isSuccess() { return success; }
    public String getErrorCode() { return errorCode; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}