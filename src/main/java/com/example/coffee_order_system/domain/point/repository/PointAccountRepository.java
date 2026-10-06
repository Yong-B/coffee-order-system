package com.example.coffee_order_system.domain.point.repository;

import com.example.coffee_order_system.domain.point.entity.PointAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PointAccountRepository
        extends JpaRepository<PointAccount, Long> {

    @Query("""
            select p
            from PointAccount p
            where p.member.id = :memberId
            """)
    Optional<PointAccount> findByMemberId(
            @Param("memberId") Long memberId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p
            from PointAccount p
            where p.member.id = :memberId
            """)
    Optional<PointAccount> findByMemberIdForUpdate(
            @Param("memberId") Long memberId
    );
}
