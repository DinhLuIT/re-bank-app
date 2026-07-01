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
import java.util.UUID;

import org.springframework.validation.BindException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import org.springframework.dao.DataIntegrityViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // BẮT LỖI NGHIỆP VỤ
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException exception) {
        ResponseCode responseCode = exception.getResponseCode();

        log.warn("Lỗi nghiệp vụ: Code = {}, Message = {}", responseCode.getCode(), responseCode.getMessage());

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

        log.warn("Lỗi Validation: {}", errors);

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
        log.warn("Đăng nhập thất bại: Sai tên đăng nhập hoặc mật khẩu");

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ResponseCode.INVALID_CREDENTIALS.getCode())
                .message(ResponseCode.INVALID_CREDENTIALS.getMessage())
                .build();

        return ResponseEntity.status(ResponseCode.INVALID_CREDENTIALS.getHttpStatus()).body(response);
    }

    // BẮT LỖI SAI METHOD HTTP (GET/POST/PUT/DELETE)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        log.warn("Phương thức HTTP không được hỗ trợ: {}", exception.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ResponseCode.METHOD_NOT_ALLOWED.getCode())
                .message(ResponseCode.METHOD_NOT_ALLOWED.getMessage())
                .build();

        return ResponseEntity.status(ResponseCode.METHOD_NOT_ALLOWED.getHttpStatus()).body(response);
    }

    // BẮT LỖI FORMAT DỮ LIỆU (VD: Truyền sai Enum, sai kiểu số)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        log.warn("Sai định dạng dữ liệu đầu vào: {}", exception.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ResponseCode.BAD_REQUEST.getCode())
                .message("Dữ liệu đầu vào không hợp lệ hoặc sai định dạng (Ví dụ: Sai giá trị ENUM)")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // BẮT LỖI SAI KIỂU DỮ LIỆU CỦA THAM SỐ (VD: Truyền 'abc' vào Enum Status trên URL)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        log.warn("Lỗi sai kiểu dữ liệu: Tham số '{}' có giá trị '{}' không thể chuyển đổi sang kiểu {}",
                exception.getName(), exception.getValue(), exception.getRequiredType() != null ? exception.getRequiredType().getSimpleName() : "Unknown");

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ResponseCode.BAD_REQUEST.getCode())
                .message("Tham số '" + exception.getName() + "' không hợp lệ (Sai định dạng hoặc giá trị không tồn tại)")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // BẮT LỖI TỪ CHỐI QUYỀN TRUY CẬP (VD: Customer gọi API Admin)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException exception) {
        log.warn("Từ chối quyền truy cập: {}", exception.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ResponseCode.FORBIDDEN.getCode())
                .message(ResponseCode.FORBIDDEN.getMessage())
                .build();

        return ResponseEntity.status(ResponseCode.FORBIDDEN.getHttpStatus()).body(response);
    }

    // BẮT LỖI TRÙNG RÀNG BUỘC DỮ LIỆU (VD: Email hoặc SĐT đã tồn tại)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        log.warn("Vi phạm ràng buộc dữ liệu (Unique Constraint): {}", exception.getMostSpecificCause().getMessage());

        ApiResponse<Void> response = ApiResponse.error(ResponseCode.DATA_ALREADY_EXISTS);

        return ResponseEntity.status(ResponseCode.DATA_ALREADY_EXISTS.getHttpStatus()).body(response);
    }

    // BẮT LỖI SAI ĐƯỜNG DẪN API (404 Not Found)
    @ExceptionHandler({
            NoHandlerFoundException.class,
            NoResourceFoundException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(Exception exception) {
        log.warn("Không tìm thấy đường dẫn API: {}", exception.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ResponseCode.ENDPOINT_NOT_FOUND.getCode())
                .message(ResponseCode.ENDPOINT_NOT_FOUND.getMessage())
                .build();

        return ResponseEntity.status(ResponseCode.ENDPOINT_NOT_FOUND.getHttpStatus()).body(response);
    }

    // BẮT LỖI HỆ THỐNG
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        String traceId = UUID.randomUUID().toString();
        log.error("Lỗi hệ thống nội bộ [TraceID: {}]: ", traceId, exception);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ResponseCode.INTERNAL_SERVER_ERROR.getCode())
                .message(ResponseCode.INTERNAL_SERVER_ERROR.getMessage() + " (TraceID: " + traceId + ")")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
