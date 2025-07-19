package letsit_backend.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import letsit_backend.CurrentUser;
import letsit_backend.dto.Response;
import letsit_backend.dto.auth.CustomOAuth2User;
import letsit_backend.dto.team.TeamCreateRequestDto;
import letsit_backend.dto.team.TeamInfoResponseDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "팀 게시판", description = "팀 생성, 조회, 종료, 탈퇴, 강퇴 관련 API")
public interface TeamApi {

    @Operation(summary = "팀 생성", description = "게시글의 작성자가 팀을 생성합니다.")
    Response<Long> createTeam(@Parameter(description = "팀을 생성할 게시글 ID") @PathVariable("postId") Long postId,
                              @Parameter(description = "팀 생성 요청 DTO") @RequestBody TeamCreateRequestDto requestDto,
                              @Parameter(hidden = true) @CurrentUser CustomOAuth2User oAuth2User);

    @Operation(summary = "팀 정보 조회", description = "팀 상세 정보를 조회합니다.")
    Response<TeamInfoResponseDto> getTeamInfo(@Parameter(description = "조회할 팀 ID") @PathVariable("teamId") Long teamId,
                                              @Parameter(hidden = true) @CurrentUser CustomOAuth2User oAuth2User);

    @Operation(summary = "프로젝트 종료", description = "팀장이 프로젝트를 종료합니다.")
    Response<?> completeTeamPost(@Parameter(description = "종료할 팀 ID") @PathVariable("teamId") Long teamId,
                                 @Parameter(hidden = true) @CurrentUser CustomOAuth2User oAuth2User);

    @Operation(summary = "팀장 탈퇴 (팀 해체)", description = "팀장이 팀을 나가며 1명인 상태로 나가면 팀이 해체됩니다.")
    Response<?> deleteTeamLeader(@Parameter(description = "탈퇴할 팀 ID") @PathVariable("teamId") Long teamId,
                                 @Parameter(hidden = true) @CurrentUser CustomOAuth2User oAuth2User);

    @Operation(summary = "팀원 강퇴", description = "팀장이 특정 팀원을 팀에서 제거합니다.")
    Response<?> deleteTeamMember(@Parameter(description = "해당 팀 ID") @PathVariable("teamId") Long teamId,
                                 @Parameter(description = "강퇴할 팀원 ID") @PathVariable("teamMemberId") Long teamMemberId,
                                 @Parameter(hidden = true) @CurrentUser CustomOAuth2User oAuth2User);
}
