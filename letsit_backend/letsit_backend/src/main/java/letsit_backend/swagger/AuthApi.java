package letsit_backend.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import letsit_backend.dto.Response;
import org.springframework.web.bind.annotation.CookieValue;

@Tag(name = "인증 관련 기능", description = "refresh 토큰 발급 및 로그아웃 API들입니다.")
public interface AuthApi {

    @Operation(summary = "토큰 재발행", description = "Refresh 토큰 발행 기능입니다.")
    Response<String> reissue(@Parameter(hidden = true) @CookieValue(name = "Refresh", required = false) String refreshToken, @Parameter(hidden = true) HttpServletResponse response);

    @Operation(summary = "로그아웃", description = "로그아웃 기능입니다.")
    Response<String> logout(@Parameter(hidden = true) HttpServletRequest request, @Parameter(hidden = true) HttpServletResponse response);
}
