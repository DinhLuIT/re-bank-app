package com.re.rebankapp.mapper;

import com.re.rebankapp.dto.response.KycAdminResponse;
import com.re.rebankapp.dto.response.KycProfileResponse;
import com.re.rebankapp.entity.KycProfile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface KycProfileMapper {
    KycProfileResponse toResponse(KycProfile kycProfile);

    KycAdminResponse toAdminResponse(KycProfile kycProfile);
}
