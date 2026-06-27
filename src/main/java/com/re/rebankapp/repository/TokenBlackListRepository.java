package com.re.rebankapp.repository;

import com.re.rebankapp.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenBlackListRepository extends JpaRepository<TokenBlacklist, Long> {

    boolean existsByAccessToken(String accessToken);
}
