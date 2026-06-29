package com.re.rebankapp.service;

import com.re.rebankapp.dto.response.UserResponseDto;
import org.springframework.data.domain.Page;

public interface UserService {

    Page<UserResponseDto> getAllUsers(int page, int size, String search);
}
