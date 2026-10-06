package com.example.coffee_order_system.domain.member.dto;

import com.example.coffee_order_system.domain.member.entity.Member;
import com.example.coffee_order_system.domain.point.entity.PointAccount;

public record MemberResponse(
        Long memberId,
        String name,
        Long pointBalance
) {

    public static MemberResponse from(
            Member member,
            PointAccount account
    ) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                account.getBalance()
        );
    }
}