package com.example.coffee_order_system.domain.member.dto;

import com.example.coffee_order_system.domain.member.entity.Member;

public record MemberResponse(
        Long memberId,
        String name,
        Long pointBalance
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getPointBalance()
        );
    }
}