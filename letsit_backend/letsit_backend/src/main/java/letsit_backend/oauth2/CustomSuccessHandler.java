package letsit_backend.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import letsit_backend.dto.auth.CustomOAuth2User;
import letsit_backend.jwt.JWTUtil;
import letsit_backend.repository.MemberRepository;
import letsit_backend.service.RedisService;
import letsit_backend.util.CookieUtil;
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
        response.addCookie(CookieUtil.createCookie("Authorization", accessToken, 60 * 60 * 6)); // 6시간
        response.addCookie(CookieUtil.createCookie("Refresh", refreshToken, 60 * 60 * 24 * 7)); // 7일
        boolean existMember = memberRepository.existsByUsername(username);

        String redirectUrl;

        if (existMember) {
            redirectUrl = "http://localhost:3000/projects"; // 기존 유저는 홈으로
        } else {
            redirectUrl = "http://localhost:3000/sign-up"; // 새 유저는 프로필 작성 페이지로
        }
        response.sendRedirect(redirectUrl);
    }
}
