package com.re.rebankapp.service.impl;

import com.re.rebankapp.dto.request.LoginRequest;
import com.re.rebankapp.dto.request.RefreshTokenRequest;
import com.re.rebankapp.dto.request.RegisterRequest;
import com.re.rebankapp.dto.request.VerifyOtpRequest;
import com.re.rebankapp.dto.request.ResetPasswordRequest;
import com.re.rebankapp.dto.response.AuthResponse;
import com.re.rebankapp.dto.response.ResetTokenResponse;
import com.re.rebankapp.entity.RefreshToken;
import com.re.rebankapp.entity.Role;
import com.re.rebankapp.entity.User;
import com.re.rebankapp.enums.RoleName;
import com.re.rebankapp.exception.AppException;
import com.re.rebankapp.exception.ResponseCode;
import com.re.rebankapp.repository.RoleRepository;
import com.re.rebankapp.repository.UserRepository;
import com.re.rebankapp.security.JwtUtils;
import com.re.rebankapp.security.UserDetailsImpl;
import com.re.rebankapp.service.AuthService;
import com.re.rebankapp.service.MailService;
import com.re.rebankapp.service.RefreshTokenService;
import com.re.rebankapp.service.TokenBlacklistService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.security.SecureRandom;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final MailService mailService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        // 1. spring security check mật khẩu
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        // 2. khi check thành công lưu vào context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. gen ra access token
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(userDetails);

        // lấy user từ db để kiểm tra role
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new AppException(ResponseCode.USER_NOT_FOUND));

        // 4. gen ra refresh token
        RefreshToken refreshTokenObj = refreshTokenService.createRefreshToken(user.getId());

        log.info("Người dùng {} đã đăng nhập thành công", user.getUsername());

        return AuthResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshTokenObj.getToken())
                .username(user.getUsername())
                .role(user.getRole().getName().name())
                .build();
    }

    @Override
    public String register(RegisterRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new AppException(ResponseCode.USER_ALREADY_EXISTS);
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setIsActive(true);
        user.setIsKyc(false);

        Role role = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new AppException(ResponseCode.ROLE_NOT_FOUND));
        user.setRole(role);

        userRepository.save(user);
        log.info("Tạo tài khoản thành công cho user: {}", user.getUsername());

        return "Đăng ký thành công";
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        return refreshTokenService.findByToken(refreshTokenRequest.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    UserDetails userDetails = UserDetailsImpl.build(user);
                    String newAccessToken = jwtUtils.generateJwtToken(userDetails);

                    log.info("Refresh token thành công cho user: {}", user.getUsername());

                    return AuthResponse.builder()
                            .accessToken(newAccessToken)
                            .refreshToken(refreshTokenRequest.getRefreshToken())
                            .username(user.getUsername())
                            .role(user.getRole().getName().name())
                            .build();
                })
                .orElseThrow(() -> new AppException(ResponseCode.REFRESH_TOKEN_NOT_FOUND));
    }

    @Override
    public void logout(String accessToken) {
        try {
            if (!tokenBlacklistService.isTokenBlacklisted(accessToken)) {
                Date expirationDate = jwtUtils.getExpirationDateFromJwtToken(accessToken);
                long expireTimeInSeconds = (expirationDate.getTime() - System.currentTimeMillis()) / 1000;

                tokenBlacklistService.blacklistToken(accessToken, expireTimeInSeconds);

                String username = jwtUtils.getUserNameFromJwtToken(accessToken);

                userRepository.findByUsername(username).ifPresent(user -> {
                    refreshTokenService.deleteByUserId(user.getId());
                    log.info("Đã xóa Refresh Token của user {}", username);
                });
            }
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Đăng xuất với token không hợp lệ hoặc đã hết hạn: {}", e.getMessage());
            // Token đã không hợp lệ thì không cần làm gì thêm, coi như đã đăng xuất
        } catch (Exception e) {
            log.error("Lỗi khi đăng xuất: ", e);
            throw new AppException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void forgotPassword(String email) {
        // Kiểm tra xem email có tồn tại không
        userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ResponseCode.USER_NOT_FOUND));

        // Sinh mã OTP ngẫu nhiên 6 chữ số
        String otp = String.format("%06d", new SecureRandom().nextInt(1000000));
        
        // Lưu OTP vào Redis với TTL là 5 phút
        stringRedisTemplate.opsForValue().set(
                "FORGOT_OTP_" + email,
                otp,
                5,
                TimeUnit.MINUTES
        );

        // Gọi MailService gửi thư
        mailService.sendOtpEmail(email, otp);
    }

    @Override
    public ResetTokenResponse verifyOtp(VerifyOtpRequest request) {
        String email = request.getEmail();
        String savedOtp = stringRedisTemplate.opsForValue().get("FORGOT_OTP_" + email);
        
        if (savedOtp == null || !savedOtp.equals(request.getOtp())) {
            throw new AppException(ResponseCode.INVALID_OTP);
        }

        // OTP đúng -> Sinh mã resetToken
        String resetToken = UUID.randomUUID().toString();
        
        // Lưu token vào Redis với TTL là 15 phút
        stringRedisTemplate.opsForValue().set(
                "RESET_TOKEN_" + email,
                resetToken,
                15,
                TimeUnit.MINUTES
        );
        
        // Xóa OTP cũ để tránh dùng lại (One-Time Password)
        stringRedisTemplate.delete("FORGOT_OTP_" + email);

        return ResetTokenResponse.builder().resetToken(resetToken).build();
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        // Kiểm tra mật khẩu xác nhận
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ResponseCode.PASSWORD_CONFIRM_MISMATCH);
        }

        String email = request.getEmail();
        String savedToken = stringRedisTemplate.opsForValue().get("RESET_TOKEN_" + email);
        
        if (savedToken == null || !savedToken.equals(request.getResetToken())) {
            throw new AppException(ResponseCode.INVALID_RESET_TOKEN);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ResponseCode.USER_NOT_FOUND));
        
        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Xóa token khỏi Redis
        stringRedisTemplate.delete("RESET_TOKEN_" + email);
        
        log.info("Người dùng {} đã đặt lại mật khẩu đăng nhập thành công", email);
    }
}
