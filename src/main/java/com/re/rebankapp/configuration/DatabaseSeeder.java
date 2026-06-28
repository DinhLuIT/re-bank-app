package com.re.rebankapp.configuration;

import com.re.rebankapp.entity.Role;
import com.re.rebankapp.entity.User;
import com.re.rebankapp.enums.RoleName;
import com.re.rebankapp.repository.RoleRepository;
import com.re.rebankapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

        // Khởi tạo tài khoản Admin mặc định
        if (userRepository.findByUsername("admin").isEmpty()) {
            Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                    .orElseThrow(() -> new RuntimeException("Role ADMIN không tồn tại"));

            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("Admin@123"));
            adminUser.setEmail("admin@rebank.com");
            adminUser.setPhoneNumber("0123456789");
            adminUser.setIsActive(true);
            adminUser.setIsKyc(true);
            adminUser.setRole(adminRole);

            userRepository.save(adminUser);
            log.info("Đã tạo tài khoản Admin mặc định (admin / Admin@123)");
        }

        log.info("Hoàn tất khởi tạo dữ liệu mẫu.");
    }
}
