package com.heukwu.userservice.email.controller.dto;

public record VerificationCodeValidateRequestDto(
        String email,
        String code
) { }
