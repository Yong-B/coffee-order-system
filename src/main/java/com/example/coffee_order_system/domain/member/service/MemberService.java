package com.example.coffee_order_system.domain.member.service;

import com.example.coffee_order_system.domain.member.dto.LoginRequest;
import com.example.coffee_order_system.domain.member.dto.MemberResponse;
import com.example.coffee_order_system.domain.member.entity.Member;
import com.example.coffee_order_system.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberResponse login(LoginRequest request) {
        Member member = memberRepository.findByLoginId(request.loginId())
                .orElseThrow(this::loginFailed);

        if (!member.getPassword().equals(request.password())) {
            throw loginFailed();
        }

        return MemberResponse.from(member);
    }

    public MemberResponse getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "로그인한 회원이 존재하지 않습니다."
                ));

        return MemberResponse.from(member);
    }

    private ResponseStatusException loginFailed() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "아이디 또는 비밀번호가 올바르지 않습니다."
        );
    }
}