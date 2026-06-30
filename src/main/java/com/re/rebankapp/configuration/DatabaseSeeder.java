package com.re.rebankapp.configuration;

import com.re.rebankapp.entity.Role;
import com.re.rebankapp.entity.User;
import com.re.rebankapp.enums.RoleName;
import com.re.rebankapp.repository.RoleRepository;
import com.re.rebankapp.repository.UserRepository;
import com.re.rebankapp.repository.AccountRepository;
import com.re.rebankapp.entity.Account;
import java.math.BigDecimal;
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
    private final AccountRepository accountRepository;
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

        // Khởi tạo tài khoản Customer mẫu có sẵn tiền để test
        if (userRepository.findByUsername("testuser").isEmpty()) {
            Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Role CUSTOMER không tồn tại"));

            User testUser = new User();
            testUser.setUsername("testuser");
            testUser.setPassword(passwordEncoder.encode("Abcd@1234"));
            testUser.setEmail("testuser@rebank.com");
            testUser.setPhoneNumber("0987654321");
            testUser.setIsActive(true);
            testUser.setIsKyc(true);
            testUser.setRole(customerRole);
            
            userRepository.save(testUser);

            Account testAccount = Account.builder()
                    .accountNumber("9999999999")
                    .balance(new BigDecimal("5000000.00")) // 5 triệu
                    .currency("VND")
                    .transactionPin(passwordEncoder.encode("123456")) // Mã PIN là 123456
                    .active(true)
                    .user(testUser)
                    .build();
            
            accountRepository.save(testAccount);

            log.info("Đã tạo tài khoản Test User (testuser / Abcd@1234) với số dư 5.000.000 VND và mã PIN: 123456");
        }

        log.info("Hoàn tất khởi tạo dữ liệu mẫu.");
    }
}
