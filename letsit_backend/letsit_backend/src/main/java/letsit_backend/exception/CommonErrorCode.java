package letsit_backend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    // 4XX : Client Error
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    // 401
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "유저 인증에 실패했습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다. 토큰 재발행을 요청해주세요."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다. 로그인을 다시 해주세요."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "인증에 필요한 JWT가 없습니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인한 사용자만 이용할 수 있는 기능입니다."),
    // 403
    FORBIDDEN(HttpStatus.FORBIDDEN, "액세스 권한이 없습니다."),
    // 404
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    POSTS_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글 정보를 찾을 수 없습니다."),

    INVALID_LOGIN_INFO(HttpStatus.BAD_REQUEST, "로그인 정보가 유효하지 않습니다."),


    // ------------------- 성공 -------------------
    // 5XX : Server Error
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "URI가 구현되어 있지 않습니다."),   // 501
    BAD_GATEWAY(HttpStatus.BAD_GATEWAY, "서버로부터의 응답이 잘못되었습니다."),       // 502
    GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "응답 대기 시간이 초과되었습니다."),

    DEFAULT(HttpStatus.INTERNAL_SERVER_ERROR, "회원 가입 오류입니다. 오류가 지속되면 문의하시기 바랍니다.");


//    // team
//    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "팀을 찾을수 없습니다."),
//    TEAM_LEADER_NOT_FOUND(HttpStatus.NOT_FOUND, "팀리더를 찾을수 없습니다."),
//    TEAM_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "팀멤버를 찾을수 없습니다."),
//    TEAM_EVALUATOR_NOT_FOUND(HttpStatus.NOT_FOUND, "평가자를 찾을수 없습니다."),
//    TEAM_EVALUATEE_NOT_FOUND(HttpStatus.NOT_FOUND, "평가받는자를 찾을수 없습니다."),
//    EVALUATION_DUPLICATED(HttpStatus.CONFLICT, "이미 평가했습니다."),
//    CALENDAR_NOT_FOUND(HttpStatus.NOT_FOUND, "캘린더 일정을 찾을수없습니다.");

    private final HttpStatus status;
    private final String message;


    @Override
    public RuntimeException getDefaultException() {
        return new CustomException(this);
    }

    @Override
    public RuntimeException getDefaultException(Throwable cause) {
        return new CustomException(this, cause);
    }
}
