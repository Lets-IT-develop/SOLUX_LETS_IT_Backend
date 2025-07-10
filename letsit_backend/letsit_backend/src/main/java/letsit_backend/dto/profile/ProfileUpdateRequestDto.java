package letsit_backend.dto.profile;

import lombok.*;
import java.util.List;

@Getter
@Builder
public class ProfileUpdateRequestDto {
    private String nickname;
    private String profileImageUrl;
    private List<String> interests;
    private String bio;
}
