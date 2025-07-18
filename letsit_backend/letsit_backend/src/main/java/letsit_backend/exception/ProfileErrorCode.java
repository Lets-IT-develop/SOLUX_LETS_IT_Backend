package letsit_backend.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProfileErrorCode implements ErrorCode {
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "프로필을 찾을수없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수가 없습니다.");

    private final HttpStatus status;
    private final String message;
}
