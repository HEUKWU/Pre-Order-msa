package com.heukwu.userservice.email.controller.dto;

import jakarta.validation.constraints.Email;

public record EmailVerificationCodeRequestDto(
        @Email
        String email
) {}
