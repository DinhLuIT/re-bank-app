package com.re.rebankapp.configuration;

import com.re.rebankapp.entity.Role;
import com.re.rebankapp.enums.RoleName;
import com.re.rebankapp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        log.info("Đang kiểm tra và khởi tạo dữ liệu mẫu (Seeding)...");

        Arrays.stream(RoleName.values()).forEach(roleName -> {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = Role.builder()
                        .name(roleName)
                        .description("Quyền " + roleName.name())
                        .build();
                roleRepository.save(role);
                log.info("Đã tạo mới Role: {}", roleName);
            }
        });

        log.info("Hoàn tất khởi tạo dữ liệu mẫu.");
    }
}
