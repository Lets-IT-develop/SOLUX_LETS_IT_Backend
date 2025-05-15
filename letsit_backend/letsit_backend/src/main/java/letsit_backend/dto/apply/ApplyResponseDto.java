package letsit_backend.dto.apply;

import letsit_backend.model.Apply;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@Builder
public class ApplyResponseDto {
    private final Long applyId;
    private final Long userId;
    private final String preferStack;
    private final String desiredField;
    private final String applyContent;
    private final String contact;
    private final Timestamp applyCreateDate;
}
