package com.re.rebankapp.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ResponseCode {

    // NHÓM MÃ THÀNH CÔNG (1xxx)
    SUCCESS(1000, "Thao tác thành công", HttpStatus.OK),
    CREATED_SUCCESS(1001, "Tạo mới thành công", HttpStatus.CREATED),
    ACCEPTED(1002, "Yêu cầu đã được tiếp nhận và đang xử lý", HttpStatus.ACCEPTED),
    DELETE_SUCCESS(1004, "Xóa dữ liệu thành công", HttpStatus.OK),

    // -----------------------------------------------------------------------------------------

    // NHÓM MÃ THẤT BẠI (4xxx)

    // LỖI LIÊN QUAN ĐẾN NGƯỜI DÙNG (40xx)
    BAD_REQUEST(4000, "Dữ liệu đầu vào không hợp lệ", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(4001, "Chưa xác thực hoặc phiên đăng nhập hết hạn", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(4003, "Bạn không có quyền thực hiện thao tác này", HttpStatus.FORBIDDEN),
    ENDPOINT_NOT_FOUND(4004, "Đường dẫn API không tồn tại", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED(4005, "Phương thức HTTP không được hỗ trợ", HttpStatus.METHOD_NOT_ALLOWED),

    // LỖI LIÊN QUAN ĐẾN BẢO MẬT & XÁC THỰC [AUTH/JWT] (41xx)
    INVALID_CREDENTIALS(4101, "Tên đăng nhập hoặc mật khẩu không chính xác", HttpStatus.BAD_REQUEST),
    ACCOUNT_LOCKED(4102, "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ CSKH", HttpStatus.FORBIDDEN),
    TOKEN_INVALID(4103, "Token không hợp lệ hoặc đã bị can thiệp", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(4104, "Token đã hết hạn, vui lòng refresh token", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_NOT_FOUND(4105, "Refresh token không tồn tại hoặc đã bị thu hồi", HttpStatus.NOT_FOUND),

    // LỖI NGƯỜI DÙNG & QUYỀN [USER & ROLE] (42xx)
    USER_NOT_FOUND(4201, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(4202, "Tên đăng nhập đã tồn tại trên hệ thống", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS(4203, "Địa chỉ email đã được đăng ký", HttpStatus.CONFLICT),
    ROLE_NOT_FOUND(4204, "Không tìm thấy quyền truy cập tương ứng", HttpStatus.NOT_FOUND),
    USER_NOT_ACTIVE(4205, "Tài khoản chưa được kích hoạt", HttpStatus.FORBIDDEN),
    DATA_ALREADY_EXISTS(4206, "Dữ liệu đã tồn tại trên hệ thống (Email hoặc Số điện thoại bị trùng)", HttpStatus.CONFLICT),

    // LỖI ĐỊNH DANH [eKYC] (43xx)
    KYC_NOT_FOUND(4301, "Người dùng chưa có hồ sơ định danh", HttpStatus.NOT_FOUND),
    KYC_ALREADY_APPROVED(4302, "Hồ sơ định danh này đã được phê duyệt", HttpStatus.BAD_REQUEST),
    KYC_REJECTED(4303, "Hồ sơ định danh không hợp lệ hoặc bị từ chối", HttpStatus.BAD_REQUEST),
    KYC_PENDING(4304, "Hồ sơ định danh đang chờ duyệt, không thể gửi lại", HttpStatus.BAD_REQUEST),
    IMAGE_UPLOAD_FAILED(4305, "Tải ảnh định danh lên hệ thống thất bại", HttpStatus.INTERNAL_SERVER_ERROR),

    // LỖI MÃ PIN GIAO DỊCH [TRANSACTION PIN] (45xx)
    INVALID_OLD_PIN(4501, "Mã PIN hiện tại không chính xác", HttpStatus.BAD_REQUEST),
    PIN_CONFIRM_MISMATCH(4502, "Mã PIN mới và xác nhận mã PIN không khớp", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD_FOR_PIN_RESET(4503, "Mật khẩu đăng nhập không chính xác, không thể đặt lại mã PIN", HttpStatus.BAD_REQUEST),
    NEW_PIN_SAME_AS_OLD(4504, "Mã PIN mới không được trùng với mã PIN hiện tại", HttpStatus.BAD_REQUEST),
    DEFAULT_PIN_NOT_ALLOWED(4505, "Vui lòng đổi mã PIN mặc định trước khi thực hiện giao dịch", HttpStatus.FORBIDDEN),
    NEW_PIN_CANNOT_BE_DEFAULT(4506, "Mã PIN mới không được là chuỗi quá đơn giản (VD: 000000, 123456...)", HttpStatus.BAD_REQUEST),

    // LỖI TÀI KHOẢN NGÂN HÀNG & GIAO DỊCH [CORE BANKING] (44xx)
    BANK_ACCOUNT_NOT_FOUND(4401, "Không tìm thấy tài khoản ngân hàng", HttpStatus.NOT_FOUND),
    INSUFFICIENT_BALANCE(4402, "Số dư tài khoản không đủ để thực hiện giao dịch", HttpStatus.CONFLICT),
    TRANSACTION_NOT_FOUND(4403, "Không tìm thấy mã giao dịch này", HttpStatus.NOT_FOUND),
    INVALID_TRANSACTION_AMOUNT(4404, "Số tiền giao dịch không hợp lệ (phải lớn hơn 0)", HttpStatus.BAD_REQUEST),
    SAME_ACCOUNT_TRANSFER(4405, "Không thể tự chuyển tiền cho chính mình", HttpStatus.BAD_REQUEST),
    DAILY_LIMIT_EXCEEDED(4406, "Giao dịch vượt quá hạn mức chuyển tiền trong ngày", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_OWNED(4407, "Tài khoản này không thuộc quyền sở hữu của bạn", HttpStatus.FORBIDDEN),
    MAX_ACCOUNTS_REACHED(4408, "Bạn đã đạt giới hạn tối đa số lượng tài khoản được phép mở", HttpStatus.BAD_REQUEST),

    // -----------------------------------------------------------------------------------------

    // NHÓM MÃ LỖI SERVER (5xxx)
    INTERNAL_SERVER_ERROR(5000, "Lỗi hệ thống nội bộ", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
