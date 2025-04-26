package letsit_backend.controller;

import jakarta.servlet.http.HttpServletResponse;
import letsit_backend.CurrentUser;
import letsit_backend.dto.auth.KakaoTokenDto;
import letsit_backend.dto.auth.LoginResponseDto;
import letsit_backend.dto.profile.ProfileRequestDto;
import letsit_backend.model.Member;
import letsit_backend.model.Profile;
import letsit_backend.service.KakaoService;
import letsit_backend.service.ProfileService;
import lombok.extern.slf4j.Slf4j;
//import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//import java.net.http.HttpHeaders;
@Slf4j
@RestController
public class KakaoController {
    @Autowired
    private KakaoService kakaoService;

    @Autowired
    private ProfileService profileService;


    @GetMapping(value = "/user/info")
    public ResponseEntity<?> ask(Authentication authentication) {
        if (authentication == null) {
            log.info("Authentication object is null.");
            return ResponseEntity.status(401).body("Unauthorized");
        }
        // 로그인정보 받아오기
        String loginId = authentication.getName();
        log.info("Logged in user: " + loginId);
        return ResponseEntity.ok("Logged in user: " + loginId);
    }




    @GetMapping("/login/oauth2/callback/kakao")
    public ResponseEntity<Map<String, Object>> KakaoLogin(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        log.info("received auth code {}", code);

        // 인가 코드를 사용하여 카카오 액세스 토큰 얻기
        KakaoTokenDto kakaoTokenDto = kakaoService.getKakaoToken(code);
        String kakaoToken = kakaoTokenDto.getAccess_token();
        log.info("received kakao access token {}", kakaoToken);


        // 카카오 액세스 토큰을 사용하여 사용자 정보 가져오기
        LoginResponseDto loginResponse = kakaoService.kakaoLogin(kakaoToken).getBody();
        log.info("received login response: {}", loginResponse);

        // JWT 토큰 생성
        String jwtToken = null;
        if (loginResponse.isLoginSuccess()) {
            jwtToken = kakaoService.getMemberByLogin(loginResponse.getMember());
            // log.info("Generated JWT Token: {}", jwtToken);

            // Set authentication to SecurityContext
            UserDetails userDetails = User.builder()
                    .username(loginResponse.getMember().getName())
                    .password("")
                    .authorities(Collections.emptyList())
                    .build();
            //security config 랑...
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            ProfileRequestDto profileDto = new ProfileRequestDto();
            profileDto.setUserId(loginResponse.getMember().getUserId());
            profileDto.setName(loginResponse.getMember().getName());
            profileDto.setAge(loginResponse.getMember().getAgeRange());
            profileDto.setProfileImageUrl(loginResponse.getMember().getProfileImageUrl());
            profileDto.setNickname("별명");
            profileDto.setBio("기본 소개");
            profileDto.setSelfIntro("자기 소개");
            profileDto.setMannerScore(75.0);
            profileDto.setMannerTier(Profile.Manner_tier.B);
            profileService.createOrUpdateProfile(profileDto, loginResponse.getMember());
        }




        // 사용자 정보와 JWT 토큰을 응답에 포함
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("user", loginResponse.getMember());
        responseBody.put("token", jwtToken);


        log.info("Returning response to frontend: {}", responseBody);
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@CurrentUser Member member) {
        if (member == null) {
            log.info("No user is currently logged in.");
            return ResponseEntity.status(401).body("Unauthorized");
        }

        // Get the access token from the authentication object or user details
        String accessToken = kakaoService.getAccessToken(member.getKakaoId());


        // Call Kakao logout API
        boolean isLoggedOutFromKakao = kakaoService.kakaoLogout(accessToken);

        if (isLoggedOutFromKakao) {
            // Invalidate the session on the server side
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok("Logout successful");
        } else {
            return ResponseEntity.status(500).body("Logout failed");
        }
    }
}
