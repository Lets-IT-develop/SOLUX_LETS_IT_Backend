package letsit_backend.model;

import jakarta.persistence.*;
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

    private String ageGroup; // 10대, 20대, 30대

    private String ageDetail; // 초, 중, 후

    private String profileImageUrl;

    private String bio;

    @ElementCollection
    private List<String> interests;

    @ElementCollection
    private List<String> skills;

    @ElementCollection
    private Map<String, String> sns;

    public Profile(Member member, String profileImageUrl, String nickname, String ageGroup, String ageDetail, List<String> Interests) {
        this.member = member;
        this.profileImageUrl = profileImageUrl;
        this.nickname = nickname;
        this.ageGroup = ageGroup;
        this.ageDetail = ageDetail;
        this.interests = Interests;
    }

    public void createSNS(Map<String, String> sns) {
        this.sns = sns;
    }
}
