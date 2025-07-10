package letsit_backend.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import letsit_backend.dto.auth.CustomOAuth2User;
import letsit_backend.jwt.JWTUtil;
import letsit_backend.repository.MemberRepository;
import letsit_backend.service.RedisService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;

@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final RedisService redisService;

    public CustomSuccessHandler(JWTUtil jwtUtil, MemberRepository memberRepository, RedisService redisService) {
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
        this.redisService = redisService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        System.out.println("✅ CustomSuccessHandler 진입");

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String username = customOAuth2User.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();

        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String accessToken = jwtUtil.createJwt(username, role, Duration.ofHours(6).toMillis()); // 6시간
        String refreshToken = jwtUtil.createRefreshToken(username, Duration.ofDays(7).toMillis()); // 7일

        redisService.setRefreshToken(username, refreshToken, Duration.ofDays(7).toMillis());

        //쿠키로 토큰 전달 후 리다이렉트
        response.addCookie(createCookie("Authorization", accessToken));
        response.addCookie(createCookie("Refresh", refreshToken));
        boolean existMember = memberRepository.existsByUsername(username);

        String redirectUrl;

        if (existMember) {
            redirectUrl = "http://localhost:3000/projects"; // 기존 유저는 홈으로
        } else {
            redirectUrl = "http://localhost:3000/sign-up"; // 새 유저는 프로필 작성 페이지로
        }
        response.sendRedirect(redirectUrl);
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(3600 * 3600);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        //cookie.setSecure(true) HTTPS에서만 전달
        //cookie.setDomain("localhost"); // 배포시 도메인 명시
        //TODO 배포시 samsite 설정 추가

        return cookie;
    }

}
