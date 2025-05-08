package letsit_backend.dto.apply;

import letsit_backend.model.Apply;
import letsit_backend.model.Member;
import letsit_backend.model.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplyRequestDto {
    private Long postId;
    private Long userId;
    private String preferStack;
    private String desiredField;
    private String applyContent;
    private String contact;

    public Apply toEntity(Post post, Member member) {
        return Apply.builder()
                .postId(post)
                .member(member)
                .preferStack(preferStack)
                .desiredField(desiredField)
                .applyContent(applyContent)
                .contact(contact)
                .build();

    }
}
