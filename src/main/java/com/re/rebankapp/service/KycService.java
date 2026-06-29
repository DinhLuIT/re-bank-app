package com.re.rebankapp.service;

import com.re.rebankapp.dto.request.KycSubmitRequest;
import com.re.rebankapp.dto.response.KycAdminResponse;
import com.re.rebankapp.dto.response.KycProfileResponse;
import com.re.rebankapp.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface KycService {

    KycProfileResponse submitKyc(Long userId, KycSubmitRequest request);

    KycProfileResponse getMyKycProfile(Long userId);

    Page<KycAdminResponse> getKycListByStatus(Status status, Pageable pageable);

    KycProfileResponse approveKyc(Long kycId);

    KycProfileResponse rejectKyc(Long kycId);
}
