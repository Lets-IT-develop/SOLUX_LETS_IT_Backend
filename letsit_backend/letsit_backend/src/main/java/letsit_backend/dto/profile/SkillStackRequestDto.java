package letsit_backend.dto.profile;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SkillStackRequestDto {
    private List<Long> skillStackIds;
}
