package letsit_backend.dto.profile;

import letsit_backend.model.Profile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequestDto {
    private String profileImageUrl;
    private String nickname;
    private String ageGroup;
    private String ageDetail;
    private List<String> interests;
}

