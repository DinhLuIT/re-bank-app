package com.re.rebankapp.service.impl;

import com.re.rebankapp.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[Rikkei Bank] Mã xác thực OTP - Quên mật khẩu");

            String htmlContent = "<h2>Xin chào,</h2>" +
                    "<p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản Rikkei Bank.</p>" +
                    "<p>Mã xác thực OTP của bạn là: <strong><span style='font-size: 24px; color: blue;'>" + otp + "</span></strong></p>" +
                    "<p>Mã này sẽ hết hạn trong vòng <strong>5 phút</strong>.</p>" +
                    "<p><i>Vui lòng không chia sẻ mã này cho bất kỳ ai!</i></p>";

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
            log.info("Email OTP đã được gửi thành công đến: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email OTP đến {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Không thể gửi email OTP, vui lòng thử lại sau.");
        }
    }
}
