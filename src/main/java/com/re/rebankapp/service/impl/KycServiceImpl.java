package com.re.rebankapp.service.impl;

import com.re.rebankapp.dto.request.KycSubmitRequest;
import com.re.rebankapp.dto.response.KycAdminResponse;
import com.re.rebankapp.dto.response.KycProfileResponse;
import com.re.rebankapp.entity.Account;
import com.re.rebankapp.entity.KycProfile;
import com.re.rebankapp.entity.User;
import com.re.rebankapp.enums.Status;
import com.re.rebankapp.exception.AppException;
import com.re.rebankapp.exception.ResponseCode;
import com.re.rebankapp.repository.AccountRepository;
import com.re.rebankapp.repository.KycProfileRepository;
import com.re.rebankapp.repository.UserRepository;
import com.re.rebankapp.service.CloudinaryService;
import com.re.rebankapp.service.KycService;
import com.re.rebankapp.mapper.KycProfileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {

    private static final String ACCOUNT_PREFIX = "8868";
    private static final int ACCOUNT_SUFFIX_LENGTH = 9;
    private static final String DEFAULT_TRANSACTION_PIN = "000000";
    private static final String DEFAULT_CURRENCY = "VND";

    private final KycProfileRepository kycProfileRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;
    private final KycProfileMapper kycProfileMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public KycProfileResponse submitKyc(Long userId, KycSubmitRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ResponseCode.USER_NOT_FOUND));

        kycProfileRepository.findByUserId(userId).ifPresent(existing -> {
            if (existing.getStatus() == Status.CONFIRM) {
                throw new AppException(ResponseCode.KYC_ALREADY_APPROVED);
            }
            if (existing.getStatus() == Status.PENDING) {
                throw new AppException(ResponseCode.KYC_PENDING);
            }
        });

        String imageUrl = cloudinaryService.uploadImage(request.getIdCardFrontImage());

        KycProfile kycProfile = KycProfile.builder()
                .idNumber(request.getIdNumber())
                .fullName(request.getFullName())
                .dob(request.getDob())
                .gender(request.getGender())
                .address(request.getAddress())
                .idCardFrontUrl(imageUrl)
                .user(user)
                .build();

        KycProfile saved = kycProfileRepository.save(kycProfile);
        return kycProfileMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public KycProfileResponse getMyKycProfile(Long userId) {
        KycProfile kycProfile = kycProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ResponseCode.KYC_NOT_FOUND));
        return kycProfileMapper.toResponse(kycProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KycAdminResponse> getKycListByStatus(Status status, Pageable pageable) {
        return kycProfileRepository.findByStatus(status, pageable)
                .map(kycProfileMapper::toAdminResponse);
    }

    @Override
    @Transactional
    public KycProfileResponse approveKyc(Long kycId) {
        KycProfile kycProfile = kycProfileRepository.findById(kycId)
                .orElseThrow(() -> new AppException(ResponseCode.KYC_NOT_FOUND));

        validatePendingStatus(kycProfile);

        kycProfile.setStatus(Status.CONFIRM);
        kycProfile.setVerifiedAt(LocalDateTime.now());

        User user = kycProfile.getUser();
        user.setIsKyc(true);
        userRepository.save(user);

        Account account = Account.builder()
                .accountNumber(generateUniqueAccountNumber())
                .balance(BigDecimal.ZERO)
                .currency(DEFAULT_CURRENCY)
                .transactionPin(passwordEncoder.encode(DEFAULT_TRANSACTION_PIN))
                .user(user)
                .build();
        accountRepository.save(account);

        log.info("Đã duyệt hồ sơ eKYC cho User [{}], cấp tài khoản thành công [{}]", user.getUsername(), account.getAccountNumber());

        return kycProfileMapper.toResponse(kycProfileRepository.save(kycProfile));
    }

    @Override
    @Transactional
    public KycProfileResponse rejectKyc(Long kycId) {
        KycProfile kycProfile = kycProfileRepository.findById(kycId)
                .orElseThrow(() -> new AppException(ResponseCode.KYC_NOT_FOUND));

        validatePendingStatus(kycProfile);

        kycProfile.setStatus(Status.REJECT);
        kycProfile.setVerifiedAt(LocalDateTime.now());

        log.info("Đã từ chối hồ sơ eKYC của User [{}]", kycProfile.getUser().getUsername());

        return kycProfileMapper.toResponse(kycProfileRepository.save(kycProfile));
    }

    private void validatePendingStatus(KycProfile kycProfile) {
        if (kycProfile.getStatus() == Status.CONFIRM) {
            throw new AppException(ResponseCode.KYC_ALREADY_APPROVED);
        }
        if (kycProfile.getStatus() == Status.REJECT) {
            throw new AppException(ResponseCode.KYC_REJECTED);
        }
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            StringBuilder sb = new StringBuilder(ACCOUNT_PREFIX);
            for (int i = 0; i < ACCOUNT_SUFFIX_LENGTH; i++) {
                sb.append(secureRandom.nextInt(10));
            }
            accountNumber = sb.toString();
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

}
