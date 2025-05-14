package letsit_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import letsit_backend.config.MapToJsonConverter;

import lombok.*;

import java.util.Map;

import static letsit_backend.model.Profile.Manner_tier.B;
import static letsit_backend.model.Profile.Manner_tier.S;

@Builder
@Getter
@Setter
@NoArgsConstructor //(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;

    @OneToOne
    @JoinColumn(name = "USER_ID")
    private Member member;

    @Enumerated(EnumType.STRING)
    private Manner_tier mannerTier = B;

    public enum Manner_tier {
        S,
        A,
        B,
        C,
        F
    }

    private double mannerScore = 75.0;

    private String name;

    private String nickname;

    private String age;

    @Lob
    @Convert(converter = MapToJsonConverter.class)
    private Map<String, String> sns;

    private String profileImageUrl;

    @Column(length = 20)
    @Size(max = 20, message = "Bio must be up to 20 characters long")
    private String bio;

    private String selfIntro;


    @Lob
    @Convert(converter = MapToJsonConverter.class)
    private Map<String, Integer> skills;

    public void mannerScoreUpdate(double mannerScore) {
        this.mannerScore = mannerScore;
    }

    public void updateMannerTier() {
        if (this.mannerScore >= 90) {
            this.mannerTier = S;
        } else if (this.mannerScore >= 80) {
            this.mannerTier = Manner_tier.A;
        } else if (this.mannerScore >= 70) {
            this.mannerTier = B;
        } else if (this.mannerScore >= 60) {
            this.mannerTier = Manner_tier.C;
        } else {
            this.mannerTier = Manner_tier.F;
        }

    }
}
