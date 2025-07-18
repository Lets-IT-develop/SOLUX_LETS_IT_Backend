package letsit_backend.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import letsit_backend.dto.auth.CustomOAuth2User;
import letsit_backend.dto.auth.MemberDto;
import letsit_backend.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private final RedisService redisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //cookie들을 불러온 뒤 Authorization Key에 담긴 쿠키를 찾음
        String accessToken = null;
        Cookie[] cookies = request.getCookies();

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            accessToken = header.substring(7);
        }

        // 쿠키에서 찾는 방식
//        if (cookies != null) {
//            for (Cookie cookie : cookies) {
//                if ("Authorization".equals(cookie.getName())) {
//                    accessToken = cookie.getValue();
//                    break; // Authorization 쿠키를 찾으면 반복 중단
//                }
//            }
//        }

        //Authorization 헤더 검증
        if (accessToken == null || jwtUtil.isExpired(accessToken) || redisService.isBlackListed(accessToken)) {
            filterChain.doFilter(request, response);

            //조건이 해당되면 메소드 종료
            return;
        }

        String username = jwtUtil.getUsername(accessToken);
        String role = jwtUtil.getRole(accessToken);
        Long userId = jwtUtil.getUserId(accessToken);

        CustomOAuth2User user = new CustomOAuth2User(
                MemberDto.builder().username(username).member(userId).role(role).build()
        );

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
