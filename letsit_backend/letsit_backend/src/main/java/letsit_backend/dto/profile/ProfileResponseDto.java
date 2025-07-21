package letsit_backend.dto.profile;

import letsit_backend.model.Interest;
import letsit_backend.model.SkillStack;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ProfileResponseDto {
    private String nickname;
    private String profileImageUrl;
    private List<Interest> interests;
    private List<SkillStack> skillStacks;
    private String bio;
    private Map<String, String> sns;
}
