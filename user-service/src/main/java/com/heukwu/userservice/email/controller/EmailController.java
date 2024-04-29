package com.heukwu.userservice.email.controller;

import com.heukwu.common.dto.ApiResponse;
import com.heukwu.userservice.email.controller.dto.EmailVerificationCodeRequestDto;
import com.heukwu.userservice.email.controller.dto.VerificationCodeValidateRequestDto;
import com.heukwu.userservice.email.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "email", description = "이메일 인증")
@RestController
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @Operation(summary = "인증 코드 이메일 전송")
    @PostMapping("/email")
    public ApiResponse<Boolean> sendVerificationEmail(@RequestBody @Valid EmailVerificationCodeRequestDto requestDto) {
        emailService.sendVerificationEmail(requestDto);

        return ApiResponse.success();
    }

    @Operation(summary = "인증 코드 검증")
    @PostMapping("/email/auth")
    public ApiResponse<Boolean> verificationEmail(@RequestBody VerificationCodeValidateRequestDto requestDto) {
        emailService.verificationCode(requestDto);

        return ApiResponse.success();
    }
}
