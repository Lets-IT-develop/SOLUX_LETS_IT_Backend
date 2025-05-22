package letsit_backend.exception;

import letsit_backend.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // 예외 메시지로 ErrorCode의 메시지 사용
        this.errorCode = errorCode;
    }

    // private final ErrorCode errorCode;
    public CustomException(String message) {
        super(message);
        this.errorCode = null;
    }

    public CustomException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }


}

