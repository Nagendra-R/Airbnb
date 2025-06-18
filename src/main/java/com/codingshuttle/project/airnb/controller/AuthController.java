package com.codingshuttle.project.airnb.controller;

import ch.qos.logback.classic.spi.IThrowableProxy;
import com.codingshuttle.project.airnb.dto.LoginRequestDto;
import com.codingshuttle.project.airnb.dto.LoginResponseDto;
import com.codingshuttle.project.airnb.dto.SignUpRequestDto;
import com.codingshuttle.project.airnb.dto.UserDto;
import com.codingshuttle.project.airnb.security.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signUp(@RequestBody SignUpRequestDto signUpRequestDto) {
        log.info("Entering sign up with email:: " + signUpRequestDto.getEmail());
        UserDto userDto = authService.signUp(signUpRequestDto);
        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        log.info("Entering login with email:: " + loginRequestDto.getEmail());
        // pass only the access token and add refresh token in cookie
        String[] tokens = authService.login(loginRequestDto);
        Cookie cookie = new Cookie("refreshToken", tokens[1]);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return ResponseEntity.ok(new LoginResponseDto(tokens[0]));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(HttpServletRequest request) {
        log.info("hit the refresh end point");
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie->"refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(()-> new AuthenticationServiceException("Refresh token not found inside the cookies"));
        String accessToken = authService.refreshToken(refreshToken);

        return ResponseEntity.ok(new LoginResponseDto(accessToken));
    }

}
