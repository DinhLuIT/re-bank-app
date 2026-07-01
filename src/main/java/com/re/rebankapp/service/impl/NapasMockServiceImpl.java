package com.re.rebankapp.service.impl;

import com.re.rebankapp.service.InterbankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class NapasMockServiceImpl implements InterbankService {

    @Override
    public boolean transferToExternalBank(String sourceAccount, String targetAccount, String bankName, BigDecimal amount) {
        log.info("Bắt đầu xử lý giao dịch liên ngân hàng qua Napas...");
        log.info("Người gửi: {}", sourceAccount);
        log.info("Người nhận: {} tại Ngân hàng {}", targetAccount, bankName);
        log.info("Số tiền: {}", amount);

        try {
            // Giả lập độ trễ mạng khi gọi API sang đối tác
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Lỗi khi kết nối đến Napas", e);
            return false;
        }

        // Giả lập từ chối giao dịch nếu số tài khoản là 999999 (Để test luồng thất bại)
        if ("999999".equals(targetAccount)) {
            log.warn("Napas từ chối giao dịch: Số tài khoản đích không hợp lệ (Mock)");
            return false;
        }

        log.info("Giao dịch liên ngân hàng thành công qua Napas.");
        return true;
    }
}
