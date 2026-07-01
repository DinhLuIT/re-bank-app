package com.re.rebankapp.service;

import java.math.BigDecimal;

public interface InterbankService {

    /**
     * Giả lập gửi yêu cầu chuyển tiền qua hệ thống trung gian (Napas)
     * @param sourceAccount Số tài khoản người gửi
     * @param targetAccount Số tài khoản người nhận
     * @param bankName Tên ngân hàng nhận
     * @param amount Số tiền chuyển
     * @return true nếu giao dịch liên ngân hàng thành công, ngược lại ném ngoại lệ hoặc trả về false
     */
    boolean transferToExternalBank(String sourceAccount, String targetAccount, String bankName, BigDecimal amount);
}
