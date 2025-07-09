package letsit_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostSoftSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postSoftSkillId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "soft_skill_id")
    private SoftSkill softSkill;

    // 연관관계 편의 생성자
    public PostSoftSkill(Post post, SoftSkill softSkill) {
        this.post = post;
        this.softSkill = softSkill;
    }

    // === 연관관계 편의 메서드 ===
    public void unlink() {
        if (softSkill != null) {
            softSkill.getPostSoftSkills().remove(this);
            softSkill = null;
        }
        if (post != null) {
            post.getPostSoftSkills().remove(this);
            post = null;
        }
    }
}
