package letsit_backend.service;

import letsit_backend.dto.team.*;
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
    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;

    /**
     * 현재 ui에서는 팀생성 로직이 따로 없음. 그냥 게시글 올리고 그 게시글에서 신청내역에서 바로 팀원받기 가능
     * 그럼 이 teamMember생성을 0이다가 1이 될때 teamPost를 생성하게 해야하는지,
     * teamPost를 생성하는 로직이 ui에 추가되고 teamPost생성 시점에 일괄로 teamMember를 생성할지
     * 전자의 경우엔 teamPost를 만들때 프로젝트title을 어디서 가져올지 and 변경기능이 있는지
     */
    // 팀 게시판 생성
    @Transactional
    public void createTeamPost(Long postId, Member member, TeamCreateRequestDto request) {
        Post post = getPost(postId);
        validatePostOwnership(member, post);

        // teamPost 객체 생성
        TeamPost teamPost = TeamPost.builder()
                .post(post)
                .prjTitle(request.getTeamName())
                .build();
        teamPostRepository.save(teamPost);

        // 리더 생성
        TeamMember teamMember = TeamMember.builder()
                .teamId(teamPost)
                .userId(member)
                .teamMemberRole(TeamMember.Role.Team_Leader)
                .build();
        teamMemberRepository.save(teamMember);

        // FIXME teamMember 객체 생성? or 승인할때마다 TeamMember생성 api 별도로 호출?
    }

    // 팀 멤버 생성 -> 지원서 승인
    @Transactional
    public void createTeamMember(Long teamId, Long memberId, Member member) {
        TeamPost teamPost = getTeamPost(teamId);
        TeamMember currentLeader = getTeamMemberByMemberAndTeamPost(member, teamPost);
        validateIsLeader(currentLeader);

        // 등록할 member조회
        Member targetMember = getMember(memberId);

        TeamMember teamMember = TeamMember.builder()
                .teamId(teamPost)
                .userId(targetMember)
                .teamMemberRole(TeamMember.Role.Team_Member)
                .build();
        teamMemberRepository.save(teamMember);
    }

    // 팀정보 조회
    @Transactional(readOnly = true)
    public TeamInfoResponseDto getTeamInfo(Long teamId, Member member) {
        TeamPost teamPost = getTeamPost(teamId);
        validateTeamMembershipPermission(member, teamPost);

        List<TeamMember> teamMemberList = teamMemberRepository.findAllByTeamPost(teamPost);
        if (teamMemberList.isEmpty()) throw new IllegalArgumentException("teamMember is not found");

        List<TeamMemberInfoDto> teamMemberInfoDtos = teamMemberList.stream()
                .map(teamMember -> {
                    Profile profile = profileRepository.findByUserId(teamMember.getUserId()); // FIXME N+1
                    if (profile == null) throw new IllegalArgumentException("profile is not found");

                    // teamMemberInfoDto 객체 생성
                    return TeamMemberInfoDto.builder()
                            .userId(teamMember.getUserId().getUserId())
                            .userName(teamMember.getUserId().getName())
                            .ProfileImageUrl(profile.getProfileImageUrl())
                            .position("정해지지않음") // FIXME 추후 논의후 변경
                            .role(teamMember.getTeamMemberRole())
                            .build();
                }).collect(Collectors.toList());

        return new TeamInfoResponseDto(teamPost.getPrjTitle(), teamMemberInfoDtos);
    }

    // 프로젝틑 종료여부 조회
    @Transactional(readOnly = true)
    public boolean isCompleted(Long teamId, Member member) {
        TeamPost teamPost = getTeamPost(teamId);
        validateTeamMembershipPermission(member, teamPost);

        return teamPost.getIsComplete();
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

        // 전체 팀원 조회(락)
        List<TeamMember> allTeamMembers = teamMemberRepository.findAllByTeamPostWithLock(teamPost);

        if (allTeamMembers.size() <=1) {
            // 팀장 혼자만 남은 경우 - 팀해체
            disbandTeam(currentLeader); // FIXME 삭제처리? or 비활성화?
        } else {
            transferleader(currentLeader, allTeamMembers);
        }
    }

    // 팀원 강퇴(팀장 only) (완료)
    @Transactional
    public void deleteTeamMember(Long teamId, Long teamMemberId, Member member) {
        TeamPost teamPost = getTeamPost(teamId);
        TeamMember currentLeader = getTeamMemberByMemberAndTeamPost(member,teamPost);
        validateIsLeader(currentLeader);

        TeamMember targetTeamMember = getTeamMember(teamMemberId);

        teamMemberRepository.delete(targetTeamMember);
    }

    /**
     * private helper methods
     */

    private Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(()-> new IllegalArgumentException("post not found"));
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(()-> new IllegalArgumentException("member not found"));
    }

    private TeamPost getTeamPost(Long teamPostId) {
        return teamPostRepository.findById(teamPostId)
                .orElseThrow(()-> new IllegalArgumentException("teamPost not found"));

    }

    private TeamMember getTeamMember(Long teamMemberId) {
        return teamMemberRepository.findById(teamMemberId)
                .orElseThrow(()-> new IllegalArgumentException("teamMember not found"));
    }

    private TeamMember getTeamMemberByMemberAndTeamPost(Member member, TeamPost teamPost) {
        return teamMemberRepository.findByMemberAndTeamPost(member,teamPost)
                .orElseThrow(()-> new IllegalArgumentException("teamMember not found"));
    }

    private void disbandTeam(TeamMember currentLeader) {
        TeamPost teamPost = currentLeader.getTeamId();
        teamMemberRepository.delete(currentLeader);
        teamPostRepository.delete(teamPost);
    }

    private void transferleader(TeamMember currentLeader, List<TeamMember> allTeamMembers) {
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
                .orElseThrow(()-> new IllegalStateException("새로운팀장 선정 불가"));
    }

    /**
     * Validator
     */

    private void validateTeamMembershipPermission(Member member, TeamPost teamPost) {
        if (!teamMemberRepository.existsByMemberAndTeamPost(member,teamPost)) {
            throw new IllegalArgumentException("teamPost Permission Denied");
        }
    }

    private void validateIsLeader(TeamMember teamMember) {
        if (!teamMember.getTeamMemberRole().equals(TeamMember.Role.Team_Leader)) {
            throw new IllegalArgumentException("TeamPost Permission Denied");
        }
    }

    private void validatePostOwnership(Member member, Post post) {
        if (!post.getUserId().getUserId().equals(member.getUserId())) {
            throw new IllegalArgumentException("post owner permission denied");
        }
    }






}
