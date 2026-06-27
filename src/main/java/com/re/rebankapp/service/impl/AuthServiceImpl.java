package com.re.rebankapp.service.impl;

import com.re.rebankapp.dto.request.LoginRequest;
import com.re.rebankapp.dto.request.RefreshTokenRequest;
import com.re.rebankapp.dto.request.RegisterRequest;
import com.re.rebankapp.dto.response.AuthResponse;
import com.re.rebankapp.entity.RefreshToken;
import com.re.rebankapp.entity.Role;
import com.re.rebankapp.entity.TokenBlacklist;
import com.re.rebankapp.entity.User;
import com.re.rebankapp.enums.RoleName;
import com.re.rebankapp.exception.AppException;
import com.re.rebankapp.exception.ResponseCode;
import com.re.rebankapp.repository.RoleRepository;
import com.re.rebankapp.repository.TokenBlackListRepository;
import com.re.rebankapp.repository.UserRepository;
import com.re.rebankapp.security.JwtUtils;
import com.re.rebankapp.security.UserDetailsImpl;
import com.re.rebankapp.service.AuthService;
import com.re.rebankapp.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenBlackListRepository tokenBlackListRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

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
        user.setPassword(registerRequest.getPassword());
        user.setEmail(registerRequest.getEmail());
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
    public void logout(String accessToken){
        if (!tokenBlackListRepository.existsByAccessToken(accessToken)) {
            Date expirationDate = jwtUtils.getExpirationDateFromJwtToken(accessToken);
            LocalDateTime expiryAt = expirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            TokenBlacklist blacklist = TokenBlacklist.builder()
                    .accessToken(accessToken)
                    .blacklistedAt(LocalDateTime.now())
                    .expiryAt(expiryAt)
                    .build();

            tokenBlackListRepository.save(blacklist);
            log.info("Đã đưa token vào Sổ đen (đăng xuất)");
        }
    }
}
