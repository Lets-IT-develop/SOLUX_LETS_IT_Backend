package letsit_backend.dto.team;

import letsit_backend.model.TeamMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class TeamInfoResponseDto {
    private String teamName;
    private List<TeamMemberInfoDto> teamMemberInfo;

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class TeamMemberInfoDto {
        private Long userId;
        private TeamMember.Role role;
        private String userName;
        private String position;
        private String ProfileImageUrl;
    }
}
