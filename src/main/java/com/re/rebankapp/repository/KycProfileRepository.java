package com.re.rebankapp.repository;

import com.re.rebankapp.entity.KycProfile;
import com.re.rebankapp.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KycProfileRepository extends JpaRepository<KycProfile, Long> {

    Optional<KycProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    @Query("SELECT k FROM KycProfile k WHERE k.status = :status")
    Page<KycProfile> findByStatus(@Param("status") Status status, Pageable pageable);
}
