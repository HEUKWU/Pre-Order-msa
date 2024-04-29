package com.heukwu.common.user;

public record LoginRequestDto(
        String username,
        String password
) { }
