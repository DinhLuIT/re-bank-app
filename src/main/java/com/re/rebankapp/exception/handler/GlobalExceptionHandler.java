package com.re.rebankapp.exception.handler;

import com.re.rebankapp.dto.response.ApiResponse;
import com.re.rebankapp.exception.AppException;
import com.re.rebankapp.exception.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

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

    // BẮT LỖI VALIDATION
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        log.warn("Validation Error: {}", errors);

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .code(ResponseCode.BAD_REQUEST.getCode())
                .message(ResponseCode.BAD_REQUEST.getMessage())
                .data(errors)
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
