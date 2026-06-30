# QUY TẮC NGHIỆP VỤ CỐT LÕI (CORE BUSINESS RULES) - RIKKEI BANK
*Bản ghi nhớ này sẽ luôn được nạp vào bộ nhớ của AI trong mọi cuộc hội thoại thuộc dự án này.*

## PHẦN 1: TÀI LIỆU SRS GỐC (BẮT BUỘC TUÂN THỦ 100%)
Dưới đây là bảng đặc tả yêu cầu phần mềm (SRS) gốc của dự án. Mọi tính năng phát triển phải bám sát bảng này, tuyệt đối không được tự ý thêm bớt quyền (Role) hay làm sai lệch luồng nghiệp vụ.

| Mã Code | Tên Chức Năng (Nghiệp vụ) | Role (Quyền truy cập) |
| :--- | :--- | :--- |
| FR-01 | Đăng nhập hệ thống (Cấp phát JWT) | Public |
| FR-02 | Xoay vòng Token (Refresh Token) | Public |
| FR-03 | Đăng xuất (Revoke Token) | Authenticated |
| FR-04 | Đăng ký mở tài khoản (Tải lên eKYC) | Public |
| FR-05 | Quản lý Người dùng & Tài khoản (CRUD, Phân trang) | Admin, Staff |
| FR-06 | Vấn tin số dư tài khoản | Customer |
| FR-07 | Chuyển tiền (Nội bộ / Liên ngân hàng) | Customer |
| FR-08 | Xem sao kê lịch sử giao dịch | Customer |
| FR-09 | Phê duyệt hồ sơ định danh (Duyệt eKYC) | Staff |
| FR-10 | Đổi mã PIN / Quên mật khẩu | Authenticated / Public |

## PHẦN 2: YÊU CẦU PHI CHỨC NĂNG (NFR) BẮT BUỘC
1. **Hiệu năng:** API giao dịch chuyển tiền phải hoàn tất $\le 5s$. API thường $\le 3s$. File upload tối đa 5MB.
2. **Bảo mật:** Mật khẩu/PIN giao dịch phải hash bằng BCrypt (strength $\ge 10$). Chuỗi JWT ký bằng Secret Key $\ge 256-bit$.
3. **Bảo trì:** Mã nguồn chia tách 3 lớp (Layered Architecture): Controller $\rightarrow$ Service $\rightarrow$ Repository.
4. **Logging:** Áp dụng kỹ thuật **AOP** (Aspect-Oriented Programming) để tự động log các thay đổi số dư, KHÔNG xâm lấn vào logic chuyển tiền.

## PHẦN 3: KIẾN TRÚC & QUY CHUẨN KỸ THUẬT
1. **Kiến trúc Đa tài khoản (1-N):**
   - 1 User có thể có nhiều Account. Không được dùng `accountRepository.findByUserId()` (trả về 1 kết quả). Phải luôn xử lý danh sách tài khoản, hoặc phải truyền chính xác `accountId` khi thao tác Vấn tin/Chuyển tiền.
2. **Truy vấn Dữ liệu:** 
   - Quản lý Khách hàng (FR-05) BẮT BUỘC áp dụng kỹ thuật **JPQL Constructor Projection** (`new DTO(...)`) để tăng hiệu năng, tuyệt đối không load toàn bộ Entity.
   - Sao kê giao dịch (FR-08) BẮT BUỘC dùng lệnh SQL/JPQL với phép toán **OR** (`from_account_id = X OR to_account_id = X`).
3. **Chuyển tiền (Transfer - Tính ACID):**
   - Phải áp dụng annotation `@Transactional`.
   - Áp dụng cơ chế **Pessimistic Locking** để chống Race Condition / Double-spending.
   - Bắt buộc kiểm tra số dư và văng ngoại lệ `InsufficientBalanceException` (HTTP 409) nếu không đủ tiền.
   - Chuyển tiền liên ngân hàng: Bắt buộc giả lập gọi API (Napas/Bên thứ 3) nếu số tài khoản không tồn tại trong hệ thống nội bộ.
4. **Chuẩn hóa API Response:**
   - URL luôn tuân thủ chuẩn RESTful danh từ số nhiều (VD: `/api/v1/accounts`).
   - Luôn trả về format `{ code, message, data }`.
   - Lỗi 401/403 phải được bắt bằng Security Filter (EntryPoint/DeniedHandler).
   - Dữ liệu rác/lỗi (Validation Errors) không được gộp vào biến `data`.

## PHẦN 4: LUỒNG ĐỊNH DANH (eKYC) VÀ TRẠNG THÁI
1. Khách hàng gọi API upload giấy tờ, tạo bản ghi `KycProfile` với trạng thái `PENDING`, cờ `isKyc = false`.
2. Hệ thống lưu ảnh lên Cloudinary/S3 và lưu `Secure URL` vào CSDL.
3. Admin/Staff duyệt hồ sơ thành `CONFIRM` $\rightarrow$ Tự động cập nhật `isKyc = true` cho User. (Nếu `REJECT`, giữ nguyên `isKyc = false`).

## PHẦN 5: THEO DÕI TIẾN ĐỘ
- Mọi tiến độ dự án được lưu trữ tại file `/Users/dinhlu2482/Desktop/Banking_Java/rebank_roadmap.html`. Luôn tham chiếu file này trước khi code.
