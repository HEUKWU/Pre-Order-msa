package com.heukwu.userservice.user.service;

import com.heukwu.common.exception.BusinessException;
import com.heukwu.common.exception.ErrorMessage;
import com.heukwu.common.exception.NotFoundException;
import com.heukwu.common.jwt.JwtUtil;
import com.heukwu.common.user.User;
import com.heukwu.common.user.UserRepository;
import com.heukwu.common.user.UserRoleEnum;
import com.heukwu.common.util.EncryptUtil;
import com.heukwu.userservice.email.entity.EmailVerificationStatusEnum;
import com.heukwu.userservice.email.repository.EmailRepository;
import com.heukwu.userservice.user.controller.dto.MyPageResponseDto;
import com.heukwu.userservice.user.controller.dto.PasswordChangeRequestDto;
import com.heukwu.userservice.user.controller.dto.SignupRequestDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailRepository emailRepository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptUtil encryptor;

    public void signup(SignupRequestDto requestDto) {
        Optional<User> findUser = userRepository.findUserByUsername(requestDto.username());
        if (findUser.isPresent()) {
            throw new BusinessException(ErrorMessage.DUPLICATE_USERNAME);
        }

        // 인증된 이메일인지 검증
        if (emailRepository.findByEmailAndVerificationStatus(requestDto.email(), EmailVerificationStatusEnum.VERIFIED).isEmpty()) {
            throw new BusinessException(ErrorMessage.UNAUTHENTICATED_EMAIL);
        }

        String encryptedEmail = encryptor.encrypt(requestDto.email());
        String encryptedPassword = passwordEncoder.encode(requestDto.password());
        String encryptedName = encryptor.encrypt(requestDto.name());
        String encryptedAddress = encryptor.encrypt(requestDto.address());

        User user = User.builder()
                .username(requestDto.username())
                .password(encryptedPassword)
                .role(UserRoleEnum.USER)
                .name(encryptedName)
                .email(encryptedEmail)
                .address(encryptedAddress)
                .phoneNumber(requestDto.phoneNumber())
                .build();

        userRepository.save(user);
    }

    public void logout(HttpServletResponse response) {
        Cookie cookie = new Cookie(JwtUtil.AUTHORIZATION_HEADER, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }

    @Transactional
    public void updateAddress(String address, User user) {
        User findUser = userRepository.findUserByUsername(user.getUsername()).orElseThrow(
                () -> new NotFoundException(ErrorMessage.NOT_FOUND_USER)
        );

        String encryptedAddress = encryptor.encrypt(address);
        findUser.updateAddress(encryptedAddress);
    }

    @Transactional
    public void updatePhoneNumber(String phoneNumber, User user) {
        User findUser = userRepository.findUserByUsername(user.getUsername()).orElseThrow(
                () -> new NotFoundException(ErrorMessage.NOT_FOUND_USER)
        );

        findUser.updatePhoneNumber(phoneNumber);
    }

    @Transactional
    public void updatePassword(PasswordChangeRequestDto requestDto, User user) {
        User findUser = userRepository.findUserByUsername(user.getUsername()).orElseThrow(
                () -> new NotFoundException(ErrorMessage.NOT_FOUND_USER)
        );

        verificationPassword(requestDto, user);

        String encodedPassword = passwordEncoder.encode(requestDto.password());
        findUser.updatePassword(encodedPassword);
    }

    // 기존 비밀번호 검증
    private void verificationPassword(PasswordChangeRequestDto requestDto, User user) {
        if (passwordEncoder.matches(requestDto.password(), user.getPassword())) {
            throw new BusinessException(ErrorMessage.WRONG_PASSWORD);
        }

        if (passwordEncoder.matches(requestDto.newPassword(), user.getPassword())) {
            throw new BusinessException(ErrorMessage.SAME_PASSWORD);
        }
    }

    public MyPageResponseDto getMyPage(User user) {
        User findUser = userRepository.findUserByUsername(user.getUsername()).orElseThrow(
                () -> new NotFoundException(ErrorMessage.NOT_FOUND_USER)
        );

        String name = encryptor.decrypt(findUser.getName());
        String email = encryptor.decrypt(findUser.getEmail());
        String address = encryptor.decrypt(findUser.getAddress());

        return MyPageResponseDto.builder()
                .name(name)
                .username(findUser.getUsername())
                .email(email)
                .address(address)
                .phoneNumber(findUser.getPhoneNumber())
                .build();
    }
}
