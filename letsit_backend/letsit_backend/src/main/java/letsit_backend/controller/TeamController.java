package letsit_backend.controller;

import letsit_backend.CurrentUser;
import letsit_backend.dto.Response;
import letsit_backend.dto.auth.CustomOAuth2User;
import letsit_backend.dto.team.*;
import letsit_backend.model.Member;
import letsit_backend.service.MemberService;
import letsit_backend.service.TeamService;
import letsit_backend.swagger.TeamApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class TeamController implements TeamApi {

    private final TeamService teamService;
    private final MemberService memberService;

    @Override
    @PostMapping("/posts/{postId}/teams")
    public Response<Long> createTeam(@PathVariable("postId") Long postId,
                                     @RequestBody TeamCreateRequestDto requestDto,
                                     @CurrentUser CustomOAuth2User oAuth2User) {

        Member member = teamService.getMemberByUsername(oAuth2User.getUsername());
        Long teamId = teamService.createTeam(postId, member, requestDto);
        return Response.success("팀 생성", teamId);
    }

    @Override
    @GetMapping("/teams/{teamId}")
    public Response<TeamInfoResponseDto> getTeamInfo(@PathVariable("teamId") Long teamId,
                                                     @CurrentUser CustomOAuth2User oAuth2User) {

        Member member = teamService.getMemberByUsername(oAuth2User.getUsername());
        TeamInfoResponseDto dto = teamService.getTeamInfo(teamId, member);
        return Response.success("팀 정보 조회", dto);
    }

    @Override
    @PatchMapping("/teams/{teamId}")
    public Response<?> completeTeamPost(@PathVariable("teamId") Long teamId,
                                        @CurrentUser CustomOAuth2User oAuth2User) {

        Member member = teamService.getMemberByUsername(oAuth2User.getUsername());
        teamService.completeTeamPost(teamId, member);
        return Response.success("프로젝트 종료", null);
    }

    @Override
    @DeleteMapping("/teams/{teamId}/team-members/me")
    public Response<?> deleteTeamLeader(@PathVariable("teamId") Long teamId,
                                        @CurrentUser CustomOAuth2User oAuth2User) {

        Member member = teamService.getMemberByUsername(oAuth2User.getUsername());
        teamService.deleteTeamLeader(teamId,member);
        return Response.success("팀 나가기 완료", null);
    }

    @Override
    @DeleteMapping("/teams/{teamId}/team-members/{teamMemberId}")
    public Response<?> deleteTeamMember(@PathVariable("teamId") Long teamId,
                                        @PathVariable("teamMemberId") Long teamMemberId,
                                        @CurrentUser CustomOAuth2User oAuth2User) {

        Member member = teamService.getMemberByUsername(oAuth2User.getUsername());
        teamService.deleteTeamMember(teamId, teamMemberId, member);
        return Response.success("팀원 강퇴 완료", null);
    }
}
