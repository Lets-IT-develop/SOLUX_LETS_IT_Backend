package letsit_backend.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements ErrorCode {
    INVALID_STACK(HttpStatus.BAD_REQUEST, "유효하지 않은 스택 이름이 포함되어 있습니다."),
    INVALID_SOFT_SKILL(HttpStatus.BAD_REQUEST, "유효하지 않은 소프트 스킬 이름이 포함되어 있습니다."),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "유효하지 않은 카테고리 이름이 포함되어 있습니다."),

    POST_ALREADY_CLOSED(HttpStatus.BAD_REQUEST, "이미 마감된 게시글입니다."),

    POST_CLOSED(HttpStatus.BAD_REQUEST, "마감된 게시글은 수정할 수 없습니다."),

    NOT_MATCHING_USER(HttpStatus.BAD_REQUEST, "게시글 작성자와 일치하지 않습니다."),

    AREA_NOT_FOUND(HttpStatus.NOT_FOUND, "지역 아이디와 일치하는 지역이 없습니다."),
    POSTS_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글이 존재하지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
