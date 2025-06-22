package letsit_backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import letsit_backend.dto.post.KoreanEnum;
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

    @Enumerated(EnumType.STRING)
    private SoftSkillSection section;

    @Column(nullable = false)
    private String softSkillName;

    @OneToMany(mappedBy = "softSkill",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PostSoftSkill> postSoftSkills = new ArrayList<>();

    @AllArgsConstructor
    @Getter
    public enum SoftSkillSection implements KoreanEnum {
        LEADERSHIP("리더쉽"),
        CRITICAL_THINKING("사고력 및 문제 해결 능력"),
        WORK_ATTITUDE("업무 태도"),
        TEAMWORK("팀워크 및 대인관계"),
        COLLABORATION_STYLE("협업 스타일");

        @JsonValue
        private final String korean;

        @JsonCreator
        public static SoftSkill.SoftSkillSection fromKorean(String korean) {
            return KoreanEnum.fromKorean(SoftSkill.SoftSkillSection.class, korean);
        }
    }

    // init용 Constructor
    public SoftSkill(String softSkillName, SoftSkillSection section) {
        this.softSkillName = softSkillName;
        this.section = section;
    }
}
