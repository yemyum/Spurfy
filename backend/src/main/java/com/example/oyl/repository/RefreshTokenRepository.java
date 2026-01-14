package com.example.oyl.repository;

import com.example.oyl.domain.RefreshToken;
import com.example.oyl.domain.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // ✅ 유효한 토큰 하나 찾기 (검증용)
    Optional<RefreshToken> findFirstByTokenHashAndRevokedFalseAndExpiresAtAfter(
            String tokenHash, LocalDateTime now);

    // ✅ 특정 토큰 revoke
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update RefreshToken r set r.revoked = true " +
            "where r.tokenHash = :hash and r.revoked = false")
    int revokeByHash(@Param("hash") String hash);

    // ✅ 특정 유저의 모든 토큰 revoke
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update RefreshToken r set r.revoked = true " +
            "where r.user = :user and r.revoked = false")
    int revokeAllByUser(@Param("user") User user);

    // ✅ 정기 청소 (만료/철회된 토큰 DB에서 삭제)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("delete from RefreshToken r " +
            "where r.revoked = true or r.expiresAt < :now")
    int deleteRevokedOrExpired(@Param("now") LocalDateTime now);

}
