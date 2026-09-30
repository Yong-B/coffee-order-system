package com.example.coffee_order_system.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank
        @Size(max = 50)
        String loginId,

        @NotBlank
        @Size(max = 100)
        String password
) {
}