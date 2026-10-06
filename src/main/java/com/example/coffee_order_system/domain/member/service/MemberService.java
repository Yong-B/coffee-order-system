package com.example.coffee_order_system.domain.member.service;

import com.example.coffee_order_system.domain.member.dto.LoginRequest;
import com.example.coffee_order_system.domain.member.dto.MemberResponse;
import com.example.coffee_order_system.domain.member.entity.Member;
import com.example.coffee_order_system.domain.member.repository.MemberRepository;
import com.example.coffee_order_system.domain.point.entity.PointAccount;
import com.example.coffee_order_system.domain.point.repository.PointAccountRepository;
import com.example.coffee_order_system.global.error.BusinessException;
import com.example.coffee_order_system.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PointAccountRepository pointAccountRepository;

    public MemberResponse login(LoginRequest request) {
        Member member = memberRepository.findByLoginId(request.loginId())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.INVALID_CREDENTIALS)
                );

        if (!member.getPassword().equals(request.password())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        PointAccount account = getPointAccount(member.getId());

        return MemberResponse.from(member, account);
    }

    public MemberResponse getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.MEMBER_NOT_FOUND)
                );

        PointAccount account = getPointAccount(memberId);

        return MemberResponse.from(member, account);
    }

    private PointAccount getPointAccount(Long memberId) {
        return pointAccountRepository.findByMemberId(memberId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.POINT_ACCOUNT_NOT_FOUND
                        )
                );
    }
}
