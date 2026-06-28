package com.re.rebankapp.controller;

import com.re.rebankapp.dto.request.LoginRequest;
import com.re.rebankapp.dto.response.ApiResponse;
import com.re.rebankapp.dto.response.AuthResponse;
import com.re.rebankapp.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .build();
    }

    @Test
    void testLogin_Success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("Admin@123");

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .username("admin")
                .role("ADMIN")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        ResponseEntity<ApiResponse<AuthResponse>> response = authController.login(loginRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1000, response.getBody().getCode());
        assertEquals("mock-access-token", response.getBody().getData().getAccessToken());
        assertEquals("admin", response.getBody().getData().getUsername());
    }

    @Test
    void testRegister_InvalidPasswordFormat() throws Exception {
        // Mật khẩu thiếu ký tự đặc biệt, thiếu số, quá ngắn -> Vi phạm @StrongPassword
        String invalidRequestJson = """
                {
                    "username": "user123",
                    "password": "123",
                    "email": "user@gmail.com",
                    "phoneNumber": "0901234567"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest());
    }
}
