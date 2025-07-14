package letsit_backend.dto.team;

import letsit_backend.model.Profile;
import letsit_backend.model.TeamMember;
import letsit_backend.model.TeamPost;
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
    private boolean isCompleted;
    private List<TeamMemberInfoDto> teamMemberInfo;

    public static TeamInfoResponseDto of (TeamPost teamPost, List<TeamMemberInfoDto> teamMemberInfoDTOs) {
        return TeamInfoResponseDto.builder()
                .teamName(teamPost.getPrjTitle())
                .isCompleted(teamPost.getIsComplete())
                .teamMemberInfo(teamMemberInfoDTOs)
                .build();
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class TeamMemberInfoDto {
        private Long userId;
        private TeamMember.Role role;
        private String userName;
        private List<String> position;
        private String profileImageUrl;

        public static TeamMemberInfoDto of(TeamMember teamMember, Profile profile) {
            return TeamMemberInfoDto.builder()
                    .userId(teamMember.getMember().getUserId())
                    .role(teamMember.getTeamMemberRole())
                    .userName(teamMember.getMember().getName())
                    .position(profile.getInterests())
                    .profileImageUrl(profile.getProfileImageUrl())
                    .build();
        }
    }
}
