package letsit_backend.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ApplyErrorCode implements ErrorCode {
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자가 작성한 신청서가 존재하지 않습니다."),
    ALREADY_APPLIED(HttpStatus.CONFLICT, "해당 구인글에 이미 지원하였습니다."),
    DEFAULT(HttpStatus.INTERNAL_SERVER_ERROR, "프로젝트 지원 처리 중 오류가 발생했습니다. 관리자에게 문의해주세요.");


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
