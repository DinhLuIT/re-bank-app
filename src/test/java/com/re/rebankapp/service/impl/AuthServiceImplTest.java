package com.re.rebankapp.service.impl;

import com.re.rebankapp.dto.request.LoginRequest;
import com.re.rebankapp.dto.request.RefreshTokenRequest;
import com.re.rebankapp.dto.request.RegisterRequest;
import com.re.rebankapp.dto.response.AuthResponse;
import com.re.rebankapp.entity.RefreshToken;
import com.re.rebankapp.entity.Role;
import com.re.rebankapp.entity.User;
import com.re.rebankapp.enums.RoleName;
import com.re.rebankapp.exception.AppException;
import com.re.rebankapp.exception.ResponseCode;
import com.re.rebankapp.repository.UserRepository;
import com.re.rebankapp.security.JwtUtils;
import com.re.rebankapp.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests cho AuthServiceImpl")
public class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        Role mockRole = new Role();
        mockRole.setName(RoleName.ADMIN);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("admin");
        mockUser.setPassword("hashedPassword");
        mockUser.setRole(mockRole);
    }

    @Test
    @DisplayName("Đăng nhập thành công")
    void testLogin_Success() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Admin@123");

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("mock-refresh-token");

        // Mock Security Context
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(userDetails)).thenReturn("mock-access-token");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
        when(refreshTokenService.createRefreshToken(anyLong())).thenReturn(refreshToken);

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertNotNull(response);
        assertEquals("mock-access-token", response.getAccessToken());
        assertEquals("mock-refresh-token", response.getRefreshToken());
        assertEquals("admin", response.getUsername());
        assertEquals("ADMIN", response.getRole());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, times(1)).generateJwtToken(userDetails);
        verify(refreshTokenService, times(1)).createRefreshToken(mockUser.getId());
    }

    @Test
    @DisplayName("Đăng nhập thất bại: Sai thông tin đăng nhập")
    void testLogin_Failure_BadCredentials() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("WrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, never()).generateJwtToken(any());
        verify(refreshTokenService, never()).createRefreshToken(anyLong());
    }

    @Test
    @DisplayName("Đăng ký thất bại: Tên đăng nhập đã tồn tại")
    void testRegister_DuplicateUsername() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("admin");
        request.setPassword("Admin@123");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> authService.register(request));
        assertEquals(ResponseCode.USER_ALREADY_EXISTS, exception.getResponseCode());

        verify(userRepository, times(1)).findByUsername(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Làm mới Token thất bại: Refresh Token không tồn tại (đã đăng xuất)")
    void testRefreshToken_NotFound_AfterLogout() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("old-logged-out-token");

        // Mô phỏng token không tồn tại trong DB (do đã bị xóa khi logout)
        when(refreshTokenService.findByToken(anyString())).thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> authService.refreshToken(request));
        assertEquals(ResponseCode.REFRESH_TOKEN_NOT_FOUND, exception.getResponseCode());

        verify(refreshTokenService, times(1)).findByToken(anyString());
        verify(jwtUtils, never()).generateJwtToken(any());
    }
}
