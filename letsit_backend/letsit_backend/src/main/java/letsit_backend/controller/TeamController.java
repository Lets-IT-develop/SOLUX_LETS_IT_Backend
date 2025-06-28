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

//    // 팀 생성 (게시물 작성자 only)
//    @PostMapping("/posts/{postId}/teams")
//    public Response<?> creatTeam(@PathVariable("postId") Long postId,
//                                 @RequestBody TeamCreateRequestDto teamCreateRequestDto,
//                                 @CurrentUser Member member) {
//
//        teamService.createTeamPost(postId, member, teamCreateRequestDto);
//        return Response.success("팀 생성", null);
//    }

    // 팀 멤버 생성 (팀장 only)
    @PostMapping("/teams/{teamId}/members/{targetMemberId}")
    public Response<?> createTeamMember(@PathVariable("teamId") Long teamId,
                                        @PathVariable("targetMemberId") Long targetMemberId,
                                        @CurrentUser Member member) {
        teamService.createTeamMember(teamId, targetMemberId, member);
        return Response.success("팀 멤버 생성" ,null);
    }


    // 팀 정보 조회
    @GetMapping("/teams/{teamId}")
    public Response<TeamInfoResponseDto> getTeamInfo(@PathVariable("teamId") Long teamId,
                                                     @CurrentUser Member member) {

        return Response.success("팀 정보 조회", teamService.getTeamInfo(teamId, member));
    }

    // 프로젝트 종료 여부 확인 (완료)
    @GetMapping("/teams/{teamId}/status")
    public Response<?> isTeamPostComplete(@PathVariable("teamId") Long teamId,
                                          @CurrentUser Member member) {
        return Response.success("프로젝트 마감여부 조회", teamService.isCompleted(teamId,member));
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
    @DeleteMapping("/teams/{teamPostId}/team-members/{teamMemberId}")
    public Response<?> deleteTeamMember(@PathVariable("teamPostId") Long teamPostId,
                                        @PathVariable("teamMemberId") Long teamMemberId,
                                        @CurrentUser Member member) {
        teamService.deleteTeamMember(teamPostId, teamMemberId, member);
        return Response.success("팀원 강퇴 완료", null);
    }



}
