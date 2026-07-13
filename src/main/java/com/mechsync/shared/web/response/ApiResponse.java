package com.mechsync.shared.web.response;

public record ApiResponse<T>(String status, T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("OK", data);
    }

    public static <T> ApiResponse<T> error(T data) {
        return new ApiResponse<>("ERROR", data);
    }
}
