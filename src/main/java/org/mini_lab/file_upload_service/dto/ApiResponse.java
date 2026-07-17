package org.mini_lab.file_upload_service.dto;

public record ApiResponse<T>(boolean success, T data, ApiError apiError) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> failure(ApiError error) {
        return new ApiResponse<>(false, null, error);
    }
}
