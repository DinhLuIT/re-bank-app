package com.re.rebankapp.controller.backoffice;

import com.re.rebankapp.dto.response.ApiResponse;
import com.re.rebankapp.dto.response.KycAdminResponse;
import com.re.rebankapp.dto.response.KycProfileResponse;
import com.re.rebankapp.dto.response.PageMeta;
import com.re.rebankapp.enums.Status;
import com.re.rebankapp.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/backoffice/kyc")
@PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
public class BackofficeKycController {

    private final KycService kycService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<KycAdminResponse>>> getKycList(
            @RequestParam(defaultValue = "PENDING") Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<KycAdminResponse> result = kycService.getKycListByStatus(status, pageable);

        PageMeta meta = PageMeta.builder()
                .currentPage(result.getNumber())
                .pageSize(result.getSize())
                .totalPage(result.getTotalPages())
                .totalElements(result.getTotalElements())
                .build();

        return ResponseEntity.ok(ApiResponse.success(result.getContent(), meta));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<KycProfileResponse>> approveKyc(@PathVariable Long id) {
        KycProfileResponse response = kycService.approveKyc(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<KycProfileResponse>> rejectKyc(@PathVariable Long id) {
        KycProfileResponse response = kycService.rejectKyc(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
