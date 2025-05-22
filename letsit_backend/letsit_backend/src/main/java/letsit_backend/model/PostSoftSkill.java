package letsit_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
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
}
