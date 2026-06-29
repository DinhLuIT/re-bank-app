package com.re.rebankapp.controller.user;

import com.re.rebankapp.dto.request.KycSubmitRequest;
import com.re.rebankapp.dto.response.ApiResponse;
import com.re.rebankapp.dto.response.KycProfileResponse;
import com.re.rebankapp.security.UserDetailsImpl;
import com.re.rebankapp.service.KycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/kyc")
@PreAuthorize("hasAuthority('CUSTOMER')")
public class KycController {

    private final KycService kycService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<KycProfileResponse>> submitKyc(
            @Valid KycSubmitRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        KycProfileResponse response = kycService.submitKyc(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-profile")
    public ResponseEntity<ApiResponse<KycProfileResponse>> getMyKycProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        KycProfileResponse response = kycService.getMyKycProfile(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
