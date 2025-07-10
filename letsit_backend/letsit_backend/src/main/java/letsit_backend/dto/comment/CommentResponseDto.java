package letsit_backend.dto.comment;

import letsit_backend.model.Comment;
import letsit_backend.repository.ProfileRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
public class CommentResponseDto {
    private Long commentId;
    private String nickname;
    private String comContent;
    private Timestamp comCreateDate;
    private Timestamp comUpdateDate;

    public CommentResponseDto(Comment comment, ProfileRepository profile) {
        this.commentId = comment.getCommentId();
        this.nickname = profile.findByMember(comment.getUserId()).getNickname();
        this.comContent = comment.getComContent();
        this.comCreateDate = comment.getComCreateDate();
        this.comUpdateDate = comment.getComUpdateDate();
    }
}


