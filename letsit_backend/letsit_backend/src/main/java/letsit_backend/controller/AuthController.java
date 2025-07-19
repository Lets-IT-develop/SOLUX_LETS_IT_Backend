package letsit_backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import letsit_backend.dto.Response;
import letsit_backend.service.AuthService;
import letsit_backend.swagger.AuthApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @PostMapping("/reissue")
    public Response<String> reissue(@CookieValue(name = "Refresh", required = false) String refreshToken, HttpServletResponse response) {
        return authService.reissueToken(refreshToken, response);
    }

    @PostMapping("/logout")
    public Response<String> logout(HttpServletRequest request, HttpServletResponse response) {
        return authService.logout(request, response);
    }
}
