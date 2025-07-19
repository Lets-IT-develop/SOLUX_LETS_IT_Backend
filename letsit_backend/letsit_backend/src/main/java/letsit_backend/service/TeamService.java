package letsit_backend.service;

import letsit_backend.dto.team.*;
import letsit_backend.exception.*;
import letsit_backend.model.*;
import letsit_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static letsit_backend.dto.team.TeamInfoResponseDto.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class TeamService {

    private final PostRepository postRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamPostRepository teamPostRepository;
    private final ProfileRepository profileRepository;
    private final ApplyRepository applyRepository;
    private final MemberRepository memberRepository;

    public Member getMemberByUsername(String username) {
        return memberRepository.findByUsername(username);
    }
    // 팀 게시판 & 팀멤버 생성 (게시글 주인 only)
    @Transactional
    public Long createTeam(Long postId, Member member, TeamCreateRequestDto request) {
        Post post = getPost(postId);
        validateIsPostOwner(member, post);

        // 팀생성
        TeamPost teamPost = TeamPost.builder()
                .post(post)
                .prjTitle(request.getTeamName())
                .build();
        TeamPost savedTeamPost = teamPostRepository.save(teamPost);

        // 팀리더 생성
        TeamMember leader = TeamMember.builder()
                .teamPost(teamPost)
                .member(member)
                .teamMemberRole(TeamMember.Role.Team_Leader)
                .build();
        teamMemberRepository.save(leader);

        // 팀원생성
        List<Apply> applyList = applyRepository.findAllByPostIdAndConfirm(post, true);
        applyList.forEach(apply -> {
            TeamMember teamMember = TeamMember.builder()
                    .teamPost(teamPost)
                    .member(apply.getMember())
                    .teamMemberRole(TeamMember.Role.Team_Member)
                    .build();
            teamMemberRepository.save(teamMember);
        });

        return savedTeamPost.getTeamId();
    }

    // 팀정보 조회
    @Transactional(readOnly = true)
    public TeamInfoResponseDto getTeamInfo(Long teamId, Member member) {
        TeamPost teamPost = getTeamPost(teamId);
        validateTeamMembershipPermission(member, teamPost);

        List<TeamMember> teamMemberList = teamMemberRepository.findAllByTeamPost(teamPost);
        if (teamMemberList.isEmpty()) throw new CustomException(TeamErrorCode.TEAM_NOT_FOUND);

        List<TeamMemberInfoDto> teamMemberInfoDTOs = teamMemberList.stream()
                .map(teamMember -> {
                    Profile profile = profileRepository.findByUserId(teamMember.getMember());
                    if (profile == null) throw new CustomException(ProfileErrorCode.PROFILE_NOT_FOUND);
                    return TeamMemberInfoDto.of(teamMember, profile);
                }).collect(Collectors.toList());

        return TeamInfoResponseDto.of(teamPost, teamMemberInfoDTOs);
    }

    // 프로젝트종료 (팀장 only)
    @Transactional
    public void completeTeamPost(Long teamId, Member member) {
        TeamPost teamPost = getTeamPost(teamId);
        TeamMember currentLeader = getTeamMemberByMemberAndTeamPost(member,teamPost);
        validateIsLeader(currentLeader);
        teamPost.updateComplete();
    }

    // 팀 나가기 (팀장 only)
    @Transactional
    public void deleteTeamLeader(Long teamId, Member member) {
        TeamPost teamPost = getTeamPost(teamId);
        TeamMember currentLeader = getTeamMemberByMemberAndTeamPost(member, teamPost);
        validateIsLeader(currentLeader);

        // 전체 팀원 조회
        List<TeamMember> allTeamMembers = teamMemberRepository.findAllByTeamPost(teamPost);

        if (allTeamMembers.size() <=1) {
            // 팀장 혼자만 남은 경우 - 팀해체
            disbandTeam(currentLeader);
        } else {
            transferLeader(currentLeader, allTeamMembers);
        }
    }

    // 팀원 강퇴 (팀장 only)
    @Transactional
    public void deleteTeamMember(Long teamId, Long teamMemberId, Member member) {
        TeamPost teamPost = getTeamPost(teamId);
        TeamMember currentLeader = getTeamMemberByMemberAndTeamPost(member,teamPost);
        validateIsLeader(currentLeader);

        TeamMember targetTeamMember = getTeamMember(teamMemberId);

        teamMemberRepository.delete(targetTeamMember);
    }

    private Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(()-> new CustomException(PostErrorCode.POSTS_NOT_FOUND));
    }

    private TeamPost getTeamPost(Long teamPostId) {
        return teamPostRepository.findById(teamPostId)
                .orElseThrow(()-> new CustomException(TeamErrorCode.TEAM_NOT_FOUND));

    }

    private TeamMember getTeamMember(Long teamMemberId) {
        return teamMemberRepository.findById(teamMemberId)
                .orElseThrow(()-> new CustomException(TeamErrorCode.TEAM_MEMBER_NOT_FOUND));
    }

    private TeamMember getTeamMemberByMemberAndTeamPost(Member member, TeamPost teamPost) {
        return teamMemberRepository.findByMemberAndTeamPost(member,teamPost)
                .orElseThrow(()-> new CustomException(TeamErrorCode.TEAM_MEMBER_NOT_FOUND));
    }

    private void disbandTeam(TeamMember currentLeader) {
        TeamPost teamPost = currentLeader.getTeamPost();
        teamMemberRepository.delete(currentLeader);
        teamPostRepository.delete(teamPost);
    }

    private void transferLeader(TeamMember currentLeader, List<TeamMember> allTeamMembers) {
        // 현재리더를 제외한 멤버목록 추출
        List<TeamMember> candidateMembers = allTeamMembers.stream()
                .filter(tm -> !tm.equals(currentLeader))
                .collect(Collectors.toList());

        TeamMember newLeader = selectNewLeader(candidateMembers);

        // 새 리더 지정
        newLeader.setTeamMemberRole(TeamMember.Role.Team_Leader);
        teamMemberRepository.save(newLeader);

        // 기존 리더 삭제
        teamMemberRepository.delete(currentLeader);
    }

    private TeamMember selectNewLeader(List<TeamMember> candidateMembers) {
        return candidateMembers.stream()
                .min(Comparator.comparing(TeamMember::getJoinedAt))
                .orElseThrow(()-> new CustomException(TeamErrorCode.TEAM_LEADER_SELECTION_FAILED));
    }

    private void validateTeamMembershipPermission(Member member, TeamPost teamPost) {
        if (!teamMemberRepository.existsByMemberAndTeamPost(member,teamPost)) {
            throw new CustomException(TeamErrorCode.TEAM_PERMISSION_DENIED);
        }
    }

    private void validateIsLeader(TeamMember teamMember) {
        if (!teamMember.getTeamMemberRole().equals(TeamMember.Role.Team_Leader)) {
            throw new CustomException(TeamErrorCode.TEAM_LEADER_PERMISSION_REQUIRED);
        }
    }

    private void validateIsPostOwner(Member member, Post post) {
        if (!post.getMember().getUserId().equals(member.getUserId())) {
            throw new CustomException(PostErrorCode.NOT_MATCHING_USER);
        }
    }
}
