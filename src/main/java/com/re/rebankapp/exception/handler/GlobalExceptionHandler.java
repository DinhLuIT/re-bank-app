package com.re.rebankapp.exception.handler;

import com.re.rebankapp.dto.response.ApiResponse;
import com.re.rebankapp.exception.AppException;
import com.re.rebankapp.exception.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.HashMap;
import java.util.Map;

import org.springframework.validation.BindException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // BẮT LỖI NGHIỆP VỤ
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException exception) {
        ResponseCode responseCode = exception.getResponseCode();

        log.warn("Business Exception: Code = {}, Message = {}", responseCode.getCode(), responseCode.getMessage());

        ApiResponse<Void> response = ApiResponse.error(responseCode);

        return ResponseEntity.status(responseCode.getHttpStatus()).body(response);
    }

    // BẮT LỖI VALIDATION (Cả JSON và Form-Data)
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(BindException exception) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            if ("typeMismatch".equals(error.getCode())) {
                errors.put(error.getField(), "Sai định dạng dữ liệu (Ví dụ: sai định dạng ngày tháng hoặc giá trị ENUM không tồn tại)");
            } else {
                errors.put(error.getField(), error.getDefaultMessage());
            }
        }

        log.warn("Validation Error: {}", errors);

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .code(ResponseCode.BAD_REQUEST.getCode())
                .message(ResponseCode.BAD_REQUEST.getMessage())
                .data(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // BẮT LỖI SAI MẬT KHẨU
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException exception) {
        log.warn("Login Failed: Wrong username or password");

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ResponseCode.INVALID_CREDENTIALS.getCode())
                .message(ResponseCode.INVALID_CREDENTIALS.getMessage())
                .build();

        return ResponseEntity.status(ResponseCode.INVALID_CREDENTIALS.getHttpStatus()).body(response);
    }

    // BẮT LỖI SAI METHOD HTTP (GET/POST/PUT/DELETE)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        log.warn("Method Not Allowed: {}", exception.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ResponseCode.METHOD_NOT_ALLOWED.getCode())
                .message(ResponseCode.METHOD_NOT_ALLOWED.getMessage())
                .build();

        return ResponseEntity.status(ResponseCode.METHOD_NOT_ALLOWED.getHttpStatus()).body(response);
    }

    // BẮT LỖI FORMAT DỮ LIỆU (VD: Truyền sai Enum, sai kiểu số)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        log.warn("Invalid Data Format: {}", exception.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ResponseCode.BAD_REQUEST.getCode())
                .message("Dữ liệu đầu vào không hợp lệ hoặc sai định dạng (Ví dụ: Sai giá trị ENUM)")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // BẮT LỖI HỆ THỐNG
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        log.error("Internal Server Error: ", exception);

        ApiResponse<Void> response = ApiResponse.error(ResponseCode.INTERNAL_SERVER_ERROR);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
