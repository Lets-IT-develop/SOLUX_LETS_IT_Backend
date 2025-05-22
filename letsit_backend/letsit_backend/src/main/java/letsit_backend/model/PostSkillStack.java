package letsit_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostSkillStack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postSkillStackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stack_id")
    private SkillStack skillStack;

    public PostSkillStack(Post post, SkillStack skillStack) {
        this.post = post;
        this.skillStack = skillStack;
    }
}
