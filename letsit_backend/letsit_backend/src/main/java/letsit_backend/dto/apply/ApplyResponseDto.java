package letsit_backend.dto.apply;

import letsit_backend.model.Apply;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
public class ApplyResponseDto {
    private Long applyId;
    private Long userId;
    private String preferStack;
    private String desiredField;
    private String applyContent;
    private String contact;
    private Timestamp applyCreateDate;

    public ApplyResponseDto(Apply apply) {
        this.applyId = apply.getApplyId();
        this.userId = apply.getMember().getUserId();
        this.preferStack = apply.getPreferStack();
        this.desiredField = apply.getDesiredField();
        this.applyContent = apply.getApplyContent();
        this.contact = apply.getContact();
        this.applyCreateDate = apply.getApplyCreateDate();
    }
}
