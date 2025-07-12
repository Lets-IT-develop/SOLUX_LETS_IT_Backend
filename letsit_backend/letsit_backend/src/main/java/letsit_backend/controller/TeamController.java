package letsit_backend.controller;

import letsit_backend.CurrentUser;
import letsit_backend.dto.Response;
import letsit_backend.dto.team.*;
import letsit_backend.model.Member;
import letsit_backend.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class TeamController {

    private final TeamService teamService;

    // 팀생성
    @PostMapping("/posts/{postId}/teams")
    public Response<Long> createTeam(@PathVariable("postId") Long postId,
                                     @RequestBody TeamCreateRequestDto requestDto,
                                     @CurrentUser Member member) {

        Long teamId = teamService.createTeamPostAndTeamMember(postId, member, requestDto);
        return Response.success("팀 생성", teamId);
    }

    // 팀 정보 조회
    @GetMapping("/teams/{teamId}")
    public Response<TeamInfoResponseDto> getTeamInfo(@PathVariable("teamId") Long teamId,
                                                     @CurrentUser Member member) {

        TeamInfoResponseDto dto = teamService.getTeamInfo(teamId, member);
        return Response.success("팀 정보 조회", dto);
    }

    // 프로젝트 종료 (팀장 Only)
    @PatchMapping("/teams/{teamId}")
    public Response<?> completeTeamPost(@PathVariable("teamId") Long teamId,
                                        @CurrentUser Member member) {

        teamService.completeTeamPost(teamId, member);
        return Response.success("프로젝트 종료", null);
    }

    // 팀 나가기(팀장 only)
    @DeleteMapping("/teams/{teamId}/team-members/me")
    public Response<?> deleteTeamLeader(@PathVariable("teamId") Long teamId,
                                        @CurrentUser Member member) {

        teamService.deleteTeamLeader(teamId,member);
        return Response.success("팀 나가기 완료", null);
    }

    // 팀원 강퇴(팀장 only)
    @DeleteMapping("/teams/{teamId}/team-members/{teamMemberId}")
    public Response<?> deleteTeamMember(@PathVariable("teamId") Long teamId,
                                        @PathVariable("teamMemberId") Long teamMemberId,
                                        @CurrentUser Member member) {
        teamService.deleteTeamMember(teamId, teamMemberId, member);
        return Response.success("팀원 강퇴 완료", null);
    }



}
