package letsit_backend.dto.profile;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ProfileResponseDto {
    private String nickname;
    private String profileImageUrl;
    private List<String> interests;
    private List<String> skills;
    private String bio;
    private Map<String, String> sns;
}
