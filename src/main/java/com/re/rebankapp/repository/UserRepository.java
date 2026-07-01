package com.re.rebankapp.repository;

import com.re.rebankapp.dto.response.UserResponseDto;
import com.re.rebankapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // lấy entity thay vì dto bởi vì spring security cần password để so sánh mật khẩu băm -> phải map trước khi trả dữ liệu về
    @EntityGraph(attributePaths = {"role"})
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT new com.re.rebankapp.dto.response.UserResponseDto(" +
            "u.id, u.username, u.email, u.phoneNumber, u.isActive, u.isKyc, r.name, u.createdAt) " +
            "FROM User u JOIN u.role r " +
            "WHERE (:search IS NULL OR :search = '' " +
            "   OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "   OR LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<UserResponseDto> findAllUsers(@Param("search") String search, Pageable pageable);
}
