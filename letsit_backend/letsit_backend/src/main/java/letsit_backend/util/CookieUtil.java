package letsit_backend.util;

import jakarta.servlet.http.Cookie;

public class CookieUtil {

    public static Cookie createCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(60*60*6);
        // TODO SameSite 직접 ResponseHeader로 설정하기

        return cookie;
    }

    // 커스터마이징 용
    public static Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    public static Cookie deleteCookie(String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        return cookie;
    }
}
