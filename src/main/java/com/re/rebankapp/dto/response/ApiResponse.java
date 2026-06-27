package com.re.rebankapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.re.rebankapp.exception.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Builder.Default
    private LocalDateTime timeStamp = LocalDateTime.now();
    private int code;
    private String message;
    private T data;
    private PageMeta meta;

    // TRẢ VỀ THÀNH CÔNG NHƯNG KHÔNG CÓ DATA (VD: LOGOUT)
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .message(ResponseCode.SUCCESS.getMessage())
                .build();
    }

    // TRẢ VỀ THÀNH CÔNG NHƯNG CÓ DATA (VD: lấy chi tiết 1 User)
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .message(ResponseCode.SUCCESS.getMessage())
                .data(data)
                .build();
    }

    // TRẢ VỀ THÀNH CÔNG KÈM PHÂN TRANG (VD: lấy danh sách giao dịch)
    public static <T> ApiResponse<T> success(T data, PageMeta meta) {
        return ApiResponse.<T>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .message(ResponseCode.SUCCESS.getMessage())
                .data(data)
                .meta(meta)
                .build();
    }

    // TRẢ VỀ LỖI
    public static <T> ApiResponse<T> error(ResponseCode responseCode){
        return ApiResponse.<T>builder()
                .code(responseCode.getCode())
                .message(responseCode.getMessage())
                .build();
    }
}
