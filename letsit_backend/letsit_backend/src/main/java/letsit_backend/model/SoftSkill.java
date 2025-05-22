package letsit_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SoftSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long softSkillId;

    @Column(nullable = false)
    private String skillName;

    @OneToMany(mappedBy = "softSkill",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PostSoftSkill> postSoftSkills = new ArrayList<>();
}
