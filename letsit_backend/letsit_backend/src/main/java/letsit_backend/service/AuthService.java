package letsit_backend.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import letsit_backend.dto.Response;
import letsit_backend.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JWTUtil jwtUtil;
    private final RedisService redisService;

    // TODO 현재 무조건 새 AccessToken 발급함 -> CurrentUser 사용해서 유저 정보 확인 뒤 발급 여부 결정하도록
    public Response<String> reissueToken(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = getCookie(request, "Refresh");

        if (refreshToken == null || jwtUtil.isExpired(refreshToken)) {
            return Response.fail("유효하지 않은 리프레시 토큰입니다.");
        }

        String username = jwtUtil.getUsername(refreshToken);
        String savedRefresh = redisService.getRefreshToken(username);

        if (!refreshToken.equals(savedRefresh)) {
            return Response.fail("토큰 불일치");
        }

        String role = jwtUtil.getRole(refreshToken);
        String newAccessToken = jwtUtil.createJwt(username, role, Duration.ofHours(6).toMillis()); // 6시간

        response.addCookie(createCookie("Authorization", newAccessToken));

        return Response.success("액세스 토큰 재발급 완료", null);
    }

    public Response<String> logout(HttpServletRequest request, HttpServletResponse response) {

        String accessToken = getCookie(request, "Authorization");

        if (accessToken != null && jwtUtil.validateToken(accessToken)) {

            String username = jwtUtil.getUsername(accessToken);
            redisService.deleteRefreshToken(username); // 리프레시 토큰 제거

            long remainingTime = jwtUtil.getRemainingTime(accessToken);
            redisService.setBlackList(accessToken, remainingTime); // 액세스 토큰 블랙리스트 처리
        }

        removeCookie("Authorization", response);
        removeCookie("Refresh", response);

        return Response.success("로그아웃 성공", null);
    }

    private String getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) return cookie.getValue();
        }
        return null;
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60 * 6); // 6시간
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }

    private void removeCookie(String key, HttpServletResponse response) {
        Cookie cookie = new Cookie(key, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
