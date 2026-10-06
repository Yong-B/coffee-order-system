package com.example.coffee_order_system.domain.point.entity;

import com.example.coffee_order_system.domain.member.entity.Member;
import com.example.coffee_order_system.global.error.BusinessException;
import com.example.coffee_order_system.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(nullable = false)
    private Long balance = 0L;

    public void charge(Long amount) {
        if (amount == null || amount <= 0) {
            throw new BusinessException(ErrorCode.INVALID_CHARGE_AMOUNT);
        }

        if (balance > Long.MAX_VALUE - amount) {
            throw new BusinessException(
                    ErrorCode.POINT_BALANCE_LIMIT_EXCEEDED
            );
        }

        balance += amount;
    }

    public void deduct(Long amount) {
        if (amount == null || amount <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "결제금액은 0보다 커야 합니다."
            );
        }

        if (balance < amount) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_POINTS);
        }

        balance -= amount;
    }
}