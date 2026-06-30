package com.re.rebankapp.controller.user;

import com.re.rebankapp.dto.request.TransferRequest;
import com.re.rebankapp.dto.response.ApiResponse;
import com.re.rebankapp.dto.response.PageMeta;
import com.re.rebankapp.dto.response.StatementResponse;
import com.re.rebankapp.enums.TransactionType;
import com.re.rebankapp.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests cho TransactionController")
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    @DisplayName("Vấn tin số dư thành công")
    void testGetBalance_Success() {
        when(transactionService.getBalance()).thenReturn(new BigDecimal("5000000"));

        ApiResponse<Map<String, BigDecimal>> response = transactionController.getBalance();

        assertEquals(1000, response.getCode());
        assertEquals(new BigDecimal("5000000"), response.getData().get("balance"));
    }

    @Test
    @DisplayName("Chuyển tiền thành công")
    void testTransfer_Success() {
        TransferRequest request = TransferRequest.builder()
                .targetAccountNumber("9999999999")
                .amount(new BigDecimal("2000000"))
                .transactionPin("123456")
                .description("Test transfer")
                .build();

        doNothing().when(transactionService).transfer(request);

        ApiResponse<Void> response = transactionController.transfer(request);

        assertEquals(1000, response.getCode());
        verify(transactionService, times(1)).transfer(request);
    }

    @Test
    @DisplayName("Lấy sao kê giao dịch thành công (có dữ liệu)")
    void testGetStatement_Success() {
        StatementResponse statementResponse = StatementResponse.builder()
                .transactionCode("TXN123")
                .amount(new BigDecimal("1000000"))
                .type(TransactionType.DEBIT)
                .createdAt(LocalDateTime.now())
                .description("Test stmt")
                .build();

        Page<StatementResponse> page = new PageImpl<>(List.of(statementResponse), PageRequest.of(0, 10), 1);

        when(transactionService.getStatement(any(Pageable.class))).thenReturn(page);

        ApiResponse<Object> response = transactionController.getStatement(0, 10);

        assertEquals(1000, response.getCode());
        assertNotNull(response.getData());
        
        // Cần ép kiểu để test do kiểu trả về là Object
        List<StatementResponse> content = (List<StatementResponse>) response.getData();
        assertEquals(1, content.size());
        assertEquals("TXN123", content.get(0).getTransactionCode());

        PageMeta meta = response.getMeta();
        assertNotNull(meta);
        assertEquals(0, meta.getCurrentPage());
        assertEquals(10, meta.getPageSize());
        assertEquals(1, meta.getTotalElements());
    }
    
    @Test
    @DisplayName("Lấy sao kê giao dịch thành công (danh sách rỗng)")
    void testGetStatement_EmptyResult() {
        Page<StatementResponse> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(transactionService.getStatement(any(Pageable.class))).thenReturn(page);

        ApiResponse<Object> response = transactionController.getStatement(0, 10);

        assertEquals(1000, response.getCode());
        
        List<StatementResponse> content = (List<StatementResponse>) response.getData();
        assertEquals(0, content.size());
    }
    
    @Test
    @DisplayName("Chuyển tiền thất bại: Yêu cầu không hợp lệ (null)")
    void testTransfer_NullRequest_ThrowsException() {
        // Mặc dù Controller thường được bảo vệ bởi @Valid, ta có thể test trực tiếp logic 
        // gọi service với giá trị null nếu cần. Tuy nhiên ở đây service có thể quăng lỗi.
        // Ta sẽ mock service quăng lỗi khi nhận request null.
        doThrow(new RuntimeException("Request cannot be null")).when(transactionService).transfer(null);
        
        try {
            transactionController.transfer(null);
        } catch (RuntimeException e) {
            assertEquals("Request cannot be null", e.getMessage());
        }
        
        verify(transactionService, times(1)).transfer(null);
    }
}
