package letsit_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
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

    // === 연관관계 편의 메서드 ===
    public void unlink() {
        if (skillStack != null) {
            skillStack.getPostStacks().remove(this);
            skillStack = null;
        }
        if (post != null) {
            post.getPostSkillStacks().remove(this);
            post = null;
        }
    }
}
