package com.re.rebankapp.service.impl;

import com.re.rebankapp.dto.request.TransferRequest;
import com.re.rebankapp.entity.Account;
import com.re.rebankapp.exception.AppException;
import com.re.rebankapp.repository.AccountRepository;
import com.re.rebankapp.repository.TransactionRepository;
import com.re.rebankapp.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests cho TransactionServiceImpl (Kiến trúc 1-N)")
class TransactionServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Account sourceAccount;
    private Account targetAccount;
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        // Giả lập Security Context cho hàm getOwnedAccount()
        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .id(1L)
                .username("testuser")
                .isActive(true)
                .build();
        
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        lenient().when(transactionRepository.sumDailyOutflow(anyLong())).thenReturn(BigDecimal.ZERO);

        // Chuẩn bị dữ liệu mẫu (Mock Data)
        sourceAccount = new Account();
        sourceAccount.setId(10L);
        sourceAccount.setAccountNumber("0987654321");
        sourceAccount.setBalance(new BigDecimal("5000000"));
        sourceAccount.setTransactionPin("encoded_pin");
        sourceAccount.setActive(true);
        sourceAccount.setIsPinChanged(true);

        targetAccount = new Account();
        targetAccount.setId(20L);
        targetAccount.setAccountNumber("9999999999");
        targetAccount.setBalance(new BigDecimal("1000000"));
        targetAccount.setActive(true);

        transferRequest = new TransferRequest();
        transferRequest.setSourceAccountId(10L);
        transferRequest.setTargetAccountNumber("9999999999");
        transferRequest.setAmount(new BigDecimal("2000000"));
        transferRequest.setTransactionPin("123456");
        transferRequest.setDescription("Chuyen tien test");
    }

    @Test
    @DisplayName("Chuyển tiền thành công (Kiến trúc 1-N)")
    void testTransfer_Success() {
        // Arrange: Mock getOwnedAccount() bằng findByIdAndUserId
        when(accountRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(sourceAccount));
        when(passwordEncoder.matches("123456", "encoded_pin")).thenReturn(true);
        when(accountRepository.findByAccountNumberForUpdate("0987654321")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumberForUpdate("9999999999")).thenReturn(Optional.of(targetAccount));

        // Act
        transactionService.transfer(transferRequest);

        // Assert
        assertEquals(new BigDecimal("3000000"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("3000000"), targetAccount.getBalance());
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Chuyển tiền thất bại: Tài khoản không thuộc sở hữu (Chống IDOR)")
    void testTransfer_AccountNotOwned() {
        // Arrange: User đang cố gắng chuyển từ tài khoản không phải của mình
        when(accountRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            transactionService.transfer(transferRequest);
        });
        assertEquals(4407, exception.getResponseCode().getCode());
        assertEquals("Tài khoản này không thuộc quyền sở hữu của bạn", exception.getResponseCode().getMessage());
    }

    @Test
    @DisplayName("Chuyển tiền thất bại: Không tìm thấy tài khoản nhận")
    void testTransfer_TargetAccountNotFound() {
        // Arrange
        when(accountRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(sourceAccount));
        when(passwordEncoder.matches("123456", "encoded_pin")).thenReturn(true);
        
        when(accountRepository.findByAccountNumberForUpdate("0987654321")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumberForUpdate("9999999999")).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            transactionService.transfer(transferRequest);
        });
        assertEquals(4401, exception.getResponseCode().getCode());
        assertEquals("Không tìm thấy tài khoản ngân hàng", exception.getResponseCode().getMessage());
    }

    @Test
    @DisplayName("Chuyển tiền thất bại: Sai mã PIN giao dịch")
    void testTransfer_InvalidPin() {
        // Arrange
        when(accountRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(sourceAccount));
        when(passwordEncoder.matches("123456", "encoded_pin")).thenReturn(false);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            transactionService.transfer(transferRequest);
        });
        assertEquals(4501, exception.getResponseCode().getCode());
        assertEquals("Mã PIN hiện tại không chính xác", exception.getResponseCode().getMessage());
    }

    @Test
    @DisplayName("Chuyển tiền thất bại: Số dư tài khoản không đủ")
    void testTransfer_InsufficientBalance() {
        // Arrange
        transferRequest.setAmount(new BigDecimal("10000000")); // Chuyển 10 triệu (vượt quá 5 triệu)
        when(accountRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(sourceAccount));
        when(passwordEncoder.matches("123456", "encoded_pin")).thenReturn(true);
        when(accountRepository.findByAccountNumberForUpdate("0987654321")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumberForUpdate("9999999999")).thenReturn(Optional.of(targetAccount));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            transactionService.transfer(transferRequest);
        });
        assertEquals(4402, exception.getResponseCode().getCode());
        assertEquals("Số dư tài khoản không đủ để thực hiện giao dịch", exception.getResponseCode().getMessage());
    }

    @Test
    @DisplayName("Chuyển tiền thất bại: Không thể chuyển cho chính mình")
    void testTransfer_SelfTransfer() {
        // Arrange
        transferRequest.setTargetAccountNumber("0987654321"); // Chuyển cho chính mình

        when(accountRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(sourceAccount));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            transactionService.transfer(transferRequest);
        });
        assertEquals(4405, exception.getResponseCode().getCode());
        assertEquals("Không thể tự chuyển tiền cho chính mình", exception.getResponseCode().getMessage());
    }
}
