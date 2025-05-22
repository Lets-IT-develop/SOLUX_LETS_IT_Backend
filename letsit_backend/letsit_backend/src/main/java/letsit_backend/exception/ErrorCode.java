package letsit_backend.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    String name();

    HttpStatus getStatus();

    String getMessage();

    default RuntimeException getDefaultException() {
        return new CustomException(this);
    }

    default RuntimeException getDefaultException(Throwable cause) {
        return new CustomException(this, cause);
    }
}
