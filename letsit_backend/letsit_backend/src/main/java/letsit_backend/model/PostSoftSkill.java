package letsit_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
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
}
