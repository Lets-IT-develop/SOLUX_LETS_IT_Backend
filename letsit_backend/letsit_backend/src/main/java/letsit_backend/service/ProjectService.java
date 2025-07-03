package letsit_backend.service;

import letsit_backend.dto.project.OngoingProjectDto;
import letsit_backend.dto.project.ProjectDto;
import letsit_backend.model.*;
import letsit_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final PostRepository postRepository;
    private final ApplyRepository applyRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ProfileRepository profileRepository;

    public List<ProjectDto> getProjectsByUserId(Member member) {
        List<Post> posts = postRepository.findByMemberAndDeadlineFalse(member);
        return posts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ProjectDto> getAppliedProjectsByUserId(Member member) {
        List<Apply> applies = applyRepository.findByUserId(member);

        return applies.stream()
                .filter(apply -> !apply.getPostId().isClosed())
                .map(apply -> convertToDto(apply.getPostId()))
                .collect(Collectors.toList());

        // TODO 그 list에서 stream()돌면서 teamPostId를 받아서 teamPost에 iscomplete가 False면 ongoing,True면 end
    }

    public List<OngoingProjectDto> getOngoingProjectsByUserId(Member member) {
        return getProjectsByCompletionStatus(member, false);
    }

    public List<OngoingProjectDto> getCompletedProjectsByUserId(Member member) {
        return getProjectsByCompletionStatus(member, true);
    }


    private OngoingProjectDto convertToOngoingProjectDto(TeamPost teamPost) {
        List<String> profileImages = teamMemberRepository.findByTeamId_TeamId(teamPost.getTeamId()).stream()
                .map(teamMember -> {
                    Profile profile = profileRepository.findByUserId(teamMember.getUserId());
                    return profile != null ? profile.getProfileImageUrl() : null;
                })
                .collect(Collectors.toList());
        return OngoingProjectDto.builder()
                .teamId(teamPost.getTeamId())
                .prjTitle(teamPost.getPrjTitle())
                .profileImages(profileImages)
                .build();
    }

    private ProjectDto convertToDto(Post post) {
        return ProjectDto.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .regionId(post.getRegion().getName())
                .subRegionId(post.getSubRegion().getName())
                .onoff(post.getOnOff().getKorean())
                .stack(post.getPostSkillStacks().stream()
                        .map(pss -> pss.getSkillStack().getStackName())
                        .toList())
                .difficulty(post.getDifficulty().getKorean())
                .userId(post.getMember().getUserId())
                .projectPeriod(post.getProjectPeriod().getKorean())
                .build();
    }


    // 프로젝트 조회 공통 로직 분리 - true/false로 구분
    private List<OngoingProjectDto> getProjectsByCompletionStatus(Member member, boolean isComplete) {
        return teamMemberRepository.findAllByUserId(member).stream()
                .map(TeamMember::getTeamId)
                .filter(team -> team.getIsComplete() == isComplete) // 팀의 완료 여부로 필터링
                .distinct()
                .map(this::convertToOngoingProjectDto)
                .collect(Collectors.toList());
    }
}

