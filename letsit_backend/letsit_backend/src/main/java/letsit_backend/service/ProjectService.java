package letsit_backend.service;

import letsit_backend.dto.auth.CustomOAuth2User;
import letsit_backend.dto.project.OngoingProjectDto;
import letsit_backend.dto.project.ProjectDto;
import letsit_backend.exception.CustomException;
import letsit_backend.exception.MemberErrorCode;
import letsit_backend.model.*;
import letsit_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final PostRepository postRepository;
    private final ApplyRepository applyRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ProfileRepository profileRepository;
    private final MemberRepository memberRepository;

    // 회원이 작성한 프로젝트 목록 조회
    public List<ProjectDto> getProjectsByUserId(CustomOAuth2User oAuth2User) {
        Member member = findMemberById(oAuth2User.getId());
        // 회원이 작성한 게시글 중에서 마감되지 않은 게시글을 조회
        List<Post> posts = postRepository.findByMemberAndDeadlineFalse(member);
        return posts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    // 회원이 신청한 프로젝트 목록 조회
    public List<ProjectDto> getAppliedProjectsByUserId(CustomOAuth2User oAuth2User) {
        Member member = findMemberById(oAuth2User.getId());
        List<Apply> applies = applyRepository.findByUserId(member);

        // 필터링: 신청한 프로젝트 중에서 마감되지 않은 게시글만 선택
        return applies.stream()
                .filter(apply -> !apply.getPostId().isClosed())
                .map(apply -> convertToDto(apply.getPostId()))
                .collect(Collectors.toList());

        // TODO 그 list에서 stream()돌면서 teamPostId를 받아서 teamPost에 iscomplete가 False면 ongoing,True면 end
    }

    // 회원이 참여 중인 프로젝트 목록 조회
    public List<OngoingProjectDto> getOngoingProjectsByUserId(CustomOAuth2User oAuth2User) {
        Member member = findMemberById(oAuth2User.getId());
        return getProjectsByCompletionStatus(member, false);
    }

    // 회원이 완료한 프로젝트 목록 조회
    public List<OngoingProjectDto> getCompletedProjectsByUserId(CustomOAuth2User oAuth2User) {
        Member member = findMemberById(oAuth2User.getId());
        return getProjectsByCompletionStatus(member, true);
    }

    // 팀 게시글을 OngoingProjectDto로 변환
    private OngoingProjectDto convertToOngoingProjectDto(TeamPost teamPost) {
        Post post = teamPost.getPost(); // 연관된 Post 가져오기
        // 팀 멤버들의 프로필 이미지 URL을 가져오기
        List<String> profileImages = teamMemberRepository.findByTeamId_TeamId(teamPost.getTeamId()).stream()
                .map(teamMember -> {
                    Profile profile = profileRepository.findByMember(teamMember.getMember());
                    return profile != null ? profile.getProfileImageUrl() : null;
                })
                .collect(Collectors.toList());
        // OngoingProjectDto 생성
        return OngoingProjectDto.builder()
                .teamId(teamPost.getTeamId())
                .prjTitle(teamPost.getPrjTitle())
                .profileImages(profileImages) // 필요 없으면 null 또는 빈 리스트로 처리
                .stack(post.getPostSkillStacks().stream()
                        .map(pss -> pss.getSkillStack().getStackName())
                        .toList())
                .categories(post.getPostCategories().stream()
                        .map(pc -> pc.getCategory().getCategoryName())
                        .toList())
                .projectStartDate(post.getProjectStartDate())
                .projectEndDate(post.getProjectEndDate())
                .progress(calculateProgress(post.getProjectStartDate(), post.getProjectEndDate())) // 진행률 계산
                .build();
    }

    // Post 객체를 ProjectDto로 변환
    private ProjectDto convertToDto(Post post) {
        // Post 객체에서 필요한 정보를 추출하여 ProjectDto로 변환
        return ProjectDto.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .regionId(post.getRegion().getName())
                .subRegionId(post.getSubRegion().getName())
                .viewCount(post.getViewCount())
                .onoff(post.getOnOff().getKorean())
                .stack(post.getPostSkillStacks().stream()
                        .map(pss -> pss.getSkillStack().getStackName())
                        .toList())
                .categories(post.getPostCategories().stream()
                        .map(pc -> pc.getCategory().getCategoryName())
                        .toList())
                .difficulty(post.getDifficulty().getKorean())
                .userId(post.getMember().getUserId())
                .projectStartDate(post.getProjectStartDate())
                .projectEndDate(post.getProjectEndDate())
                .build();
    }


    // 프로젝트 조회 공통 로직 분리 - true/false로 구분
    private List<OngoingProjectDto> getProjectsByCompletionStatus(Member member, boolean isComplete) {
        // 팀 멤버를 통해 팀 게시글을 조회하고, 완료 여부로 필터링
        return teamMemberRepository.findAllByUserId(member).stream()
                .map(TeamMember::getTeamPost)
                .filter(team -> team.getIsComplete() == isComplete) // 팀의 완료 여부로 필터링
                .distinct()
                .map(this::convertToOngoingProjectDto)
                .collect(Collectors.toList());
    }

    // 진행률 계산
    private Long calculateProgress(LocalDate start, LocalDate end) {
        LocalDate today = LocalDate.now();

        if (today.isBefore(start)) return 0L;            // 아직 시작 전
        if (today.isAfter(end)) return 100L;             // 이미 완료

        long totalDays = start.until(end).getDays();     // 총 기간
        long elapsedDays = start.until(today).getDays(); // 지난 기간

        if (totalDays == 0) return 100L;                 // 시작일 = 종료일 (1일짜리 프로젝트)

        long progress = Math.round((elapsedDays * 100.0) / totalDays);
        return Math.min(progress, 100L);                 // 100% 이상은 안 되게 제한
    }

    // findByX
    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

}

