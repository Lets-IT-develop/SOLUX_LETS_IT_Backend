package letsit_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    // 리프레시 토큰 저장
    public void setRefreshToken(String username, String refreshToken, long duration) {
        redisTemplate.opsForValue().set("RT:" + username, refreshToken, duration, TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(String username) {
        return redisTemplate.opsForValue().get("RT:" + username);
    }

    public void deleteRefreshToken(String username) {
        redisTemplate.delete("RT:" + username);
    }

    // 블랙리스트 저장
    public void setBlackList(String token, long duration) {
        redisTemplate.opsForValue().set("BL:" + token, "logout", duration, TimeUnit.MILLISECONDS);
    }

    public boolean isBlackListed(String token) {
        return redisTemplate.hasKey("BL:" + token);
    }
}
