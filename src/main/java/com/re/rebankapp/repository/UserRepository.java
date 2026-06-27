package com.re.rebankapp.repository;

import com.re.rebankapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // lấy entity thay vì dto bởi vì spring security cần password để so sánh mật khẩu băm -> phải map trước khi trả dữ liệu về
    Optional<User> findByUsername(String username);
}
