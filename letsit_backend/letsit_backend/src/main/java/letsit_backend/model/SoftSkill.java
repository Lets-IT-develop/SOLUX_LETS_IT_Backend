package letsit_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoftSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long softSkillId;

    @Column(nullable = false)
    private String skillName;

    @OneToMany(mappedBy = "softSkill")
    private List<PostSoftSkill> postSoftSkills = new ArrayList<>();
}
