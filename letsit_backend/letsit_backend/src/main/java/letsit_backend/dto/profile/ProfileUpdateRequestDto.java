package letsit_backend.dto.profile;

import letsit_backend.model.Interest;
import lombok.*;
import java.util.List;

@Getter
@Builder
public class ProfileUpdateRequestDto {
    private String nickname;
    private String profileImageUrl;
    private List<Interest> interests;
    private String bio;
}
