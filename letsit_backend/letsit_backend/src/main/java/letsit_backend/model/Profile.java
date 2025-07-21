package letsit_backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import letsit_backend.dto.post.KoreanEnum;
import lombok.*;

import java.util.List;
import java.util.Map;


@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;

    @OneToOne
    @JoinColumn(name = "USER_ID")
    private Member member;

    private String name;

    private String nickname;

    @Enumerated(EnumType.STRING)
    private AgeGroup ageGroup;

    @Enumerated(EnumType.STRING)
    private AgeGroupDetail ageGroupDetail;

    private String profileImageUrl;

    private String bio;

    @OneToMany
    @JoinColumn(name = "PROFILE_ID")
    private List<Interest> interests;

    @OneToMany
    @JoinColumn(name = "PROFILE_ID")
    private List<SkillStack> skillStacks;

    @OneToMany
    @JoinColumn(name = "PROFILE_ID")
    private List<SoftSkill> softSkills;

    @ElementCollection
    private Map<String, String> sns;

    // 연령대
    @AllArgsConstructor
    @Getter
    public enum AgeGroup implements KoreanEnum {
        S10("10대"),
        S20("20대"),
        S30("30대"),
        S40("40대 이상");

        @JsonValue
        private final String korean;

        @JsonCreator
        public static Post.AgeGroup fromKorean(String korean) {
            return KoreanEnum.fromKorean(Post.AgeGroup.class, korean);
        }
    }

    // 연령대 상세 (초반, 중반, 후반)
    @AllArgsConstructor
    @Getter
    public enum AgeGroupDetail implements KoreanEnum {
        EARLY("초반"),
        MID("중반"),
        LATE("후반");

        @JsonValue
        private final String korean;

        @JsonCreator
        public static Post.AgeGroupDetail fromKorean(String korean) {
            return KoreanEnum.fromKorean(Post.AgeGroupDetail.class, korean);
        }
    }

    public Profile(Member member, String profileImageUrl, String nickname, AgeGroup ageGroup, AgeGroupDetail ageDetail, List<SoftSkill> softSkills, List<Interest> interests) {
        this.member = member;
        this.profileImageUrl = profileImageUrl;
        this.nickname = nickname;
        this.ageGroup = ageGroup;
        this.ageGroupDetail = ageDetail;
        this.interests = interests;
        this.softSkills = softSkills;
    }

    public void createSNS(Map<String, String> sns) {
        this.sns = sns;
    }
}
