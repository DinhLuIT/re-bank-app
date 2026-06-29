package com.re.rebankapp.controller.admin;

import com.re.rebankapp.dto.response.ApiResponse;
import com.re.rebankapp.dto.response.PageMeta;
import com.re.rebankapp.dto.response.UserResponseDto;
import com.re.rebankapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        Page<UserResponseDto> result = userService.getAllUsers(page, size, search);

        PageMeta meta = PageMeta.builder()
                .currentPage(result.getNumber())
                .pageSize(result.getSize())
                .totalPage(result.getTotalPages())
                .totalElements(result.getTotalElements())
                .build();

        return ResponseEntity.ok(ApiResponse.success(result.getContent(), meta));
    }
}
