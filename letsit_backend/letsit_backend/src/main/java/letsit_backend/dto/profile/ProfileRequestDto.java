package letsit_backend.dto.profile;

import jakarta.validation.constraints.NotNull;
import letsit_backend.model.Interest;
import letsit_backend.model.Profile;
import letsit_backend.model.SoftSkill;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileRequestDto {
    private String profileImageUrl;

    @NotNull(message = "별명 입력은 필수입니다.")
    private String nickname;

    @NotNull(message = "연령대는 필수입니다.")
    private Profile.AgeGroup ageGroup;
    private Profile.AgeGroupDetail ageGroupDetail;

    @NotNull(message = "관심분야 선택은 필수입니다.")
    private List<Interest> interests;

    @NotNull(message = "소프트 스킬 선택은 필수입니다.")
    private List<SoftSkill> softSkills;
}

