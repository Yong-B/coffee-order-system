package com.example.coffee_order_system.domain.member.controller;

import com.example.coffee_order_system.domain.member.dto.LoginRequest;
import com.example.coffee_order_system.domain.member.dto.MemberResponse;
import com.example.coffee_order_system.domain.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private static final String LOGIN_MEMBER_ID = "LOGIN_MEMBER_ID";

    private final MemberService memberService;

    @PostMapping("/login")
    public MemberResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        MemberResponse response = memberService.login(request);

        HttpSession session = httpRequest.getSession();
        httpRequest.changeSessionId();
        session.setAttribute(LOGIN_MEMBER_ID, response.memberId());

        return response;
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
    }

    @GetMapping("/me")
    public MemberResponse me(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            throw loginRequired();
        }

        Long memberId = (Long) session.getAttribute(LOGIN_MEMBER_ID);

        if (memberId == null) {
            throw loginRequired();
        }

        return memberService.getMember(memberId);
    }

    private ResponseStatusException loginRequired() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "로그인이 필요합니다."
        );
    }
}