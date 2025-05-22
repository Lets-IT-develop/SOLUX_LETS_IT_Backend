package letsit_backend.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    String name();

    HttpStatus getStatus();

    String getMessage();

    RuntimeException getDefaultException();

    RuntimeException getDefaultException(Throwable cause);
}
