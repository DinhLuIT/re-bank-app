package com.re.rebankapp.service.impl;

import com.re.rebankapp.dto.response.UserResponseDto;
import com.re.rebankapp.repository.UserRepository;
import com.re.rebankapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<UserResponseDto> result = userRepository.findAllUsers(search, pageable);

        log.info("Truy vấn danh sách người dùng: trang={}, kích thước={}, từ khóa='{}', tổng kết quả={}",
                page, size, search != null ? search : "", result.getTotalElements());

        return result;
    }
}
