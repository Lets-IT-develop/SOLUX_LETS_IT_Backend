package letsit_backend.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode ERROR_CODE;

    public CustomException(ErrorCode ERROR_CODE) {
        super(ERROR_CODE.getMessage()); // 예외 메시지로 ErrorCode의 메시지 사용
        this.ERROR_CODE = ERROR_CODE;
    }

    // private final ErrorCode errorCode;
    public CustomException(String message) {
        super(message);
        this.ERROR_CODE = null;
    }

    public CustomException(String message, Throwable cause) {
        super(message, cause);
        this.ERROR_CODE = null;
    }


}

