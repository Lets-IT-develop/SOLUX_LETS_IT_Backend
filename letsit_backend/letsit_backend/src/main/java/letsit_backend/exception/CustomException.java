package letsit_backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;


    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CustomException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    /**
     * 예외 발생 시 에러 코드를 포함시키지 않을 경우 아래 메서드들로 처리: default 에러 코드로 통합
     */

    public CustomException() {
        this.errorCode = getDefaultErrorCode();
    }

    public CustomException(String message) {
        super(message);
        this.errorCode = getDefaultErrorCode();
    }

    public CustomException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = getDefaultErrorCode();
    }


    private static ErrorCode getDefaultErrorCode() {
        return DefaultErrorCodeHolder.DEFAULT_ERROR_CODE;
    }

    private static class DefaultErrorCodeHolder {
        private static final ErrorCode DEFAULT_ERROR_CODE = new ErrorCode() {
            @Override
            public String name() {
                return "SERVER_ERROR";
            }

            @Override
            public HttpStatus getStatus() {
                return HttpStatus.INTERNAL_SERVER_ERROR;
            }

            @Override
            public String getMessage() {
                return "서버 오류입니다. 담당자에게 문의해주세요.";
            }

            @Override
            public RuntimeException getDefaultException() {
                return new CustomException("SERVER_ERROR");
            }

            @Override
            public RuntimeException getDefaultException(Throwable cause) {
                return new CustomException("SERVER_ERROR", cause);
            }
        };
    }
}

