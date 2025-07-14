package letsit_backend.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TeamErrorCode implements ErrorCode {
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "팀게시판을 찾을수 없습니다."),
    TEAM_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "팀멤버를 찾을수없습니다."),

    TEAM_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "팀 접근 권한이 없습니다."),
    TEAM_LEADER_PERMISSION_REQUIRED(HttpStatus.FORBIDDEN, "팀장 권한이 필요합니다."),

    TEAM_LEADER_SELECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "새로운 팀장을 선정할 수 없습니다.");


    private final HttpStatus status;
    private final String message;
}
