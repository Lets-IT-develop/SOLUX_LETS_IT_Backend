package letsit_backend.dto.profile;

import letsit_backend.model.Profile;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileRequestDto {
    private String profileImageUrl;
    private String nickname;
    private String ageGroup;
    private String ageDetail;
    private List<String> interests;
}

