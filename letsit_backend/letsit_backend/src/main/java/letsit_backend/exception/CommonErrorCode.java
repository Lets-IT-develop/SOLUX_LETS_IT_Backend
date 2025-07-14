package letsit_backend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    /**
     * 4XX : Client Error
     */
    // 인증 / 인가
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // 요청 오류
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "요청값이 올바르지 않습니다."),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "필수 요청값이 누락되었습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 요청 방식입니다."),

    /**
     * 5XX : Server Error
     */
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "URI가 구현되어 있지 않습니다."),
    BAD_GATEWAY(HttpStatus.BAD_GATEWAY, "서버로부터의 응답이 잘못되었습니다."),
    GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "응답 대기 시간이 초과되었습니다."),

    // 시스템 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 오류가 발생했습니다."),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 처리 중 오류가 발생했습니다."),
    IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "입출력 처리 중 오류가 발생했습니다."),

    DEFAULT(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다. 담당자에게 문의해주세요."),

    private final HttpStatus status;
    private final String message;
}
