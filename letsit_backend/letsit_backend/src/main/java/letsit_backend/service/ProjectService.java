package letsit_backend.service;

import letsit_backend.dto.project.OngoingProjectDto;
import letsit_backend.dto.project.ProjectDto;
import letsit_backend.model.*;
import letsit_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final TeamPostRepository teamPostRepository;
    private final ApplyRepository applyRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ProfileRepository profileRepository;


    @Autowired
    public ProjectService(PostRepository postRepository, MemberRepository memberRepository, TeamPostRepository teamPostRepository, ApplyRepository applyRepository, TeamMemberRepository teamMemberRepository, ProfileRepository profileRepository) {
        this.postRepository = postRepository;
        this.memberRepository = memberRepository;
        this.teamPostRepository = teamPostRepository;
        this.applyRepository = applyRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.profileRepository = profileRepository;
    }

    public List<ProjectDto> getProjectsByUserId(Member member) {
        Member user = memberRepository.findById(member.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + member.getUserId()));
        List<Post> posts = postRepository.findByUserIdAndDeadlineFalse(user);
        return posts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    public List<ProjectDto> getAppliedProjectsByUserId(Member member) {
        Member user = memberRepository.findById(member.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + member.getUserId()));
        List<Apply> applies = applyRepository.findByUserId(user);

        return applies.stream()
                .filter(apply -> !apply.getPostId().isClosed())
                .map(apply ->  convertToDto(apply.getPostId()))
                .collect(Collectors.toList());

        // TODO 그 list에서 stream()돌면서 teamPostId를 받아서 teamPost에 iscomplete가 False면 ongoing,True면 end
    }

    public List<OngoingProjectDto> getOngoingProjectsByUserId(Member member) {
        // Fetch the list of team members for the given user
        List<TeamMember> teamMemberList = teamMemberRepository.findAllByUserId(member);

        // Filter and map to OngoingProjectDto
        List<OngoingProjectDto> ongoingProjectDtoList = teamMemberList.stream()
                .filter(teamMember -> !teamMember.getTeamId().getIsComplete()) // Corrected the filter condition
                .map(teamMember -> {
                    // Fetch all team members for the team
                    List<TeamMember> teamMembers = teamMemberRepository.findAllByTeamId(teamMember.getTeamId());

                    // Collect profile images of team members
                    List<String> profileImages = teamMembers.stream()
                            .map(tm -> tm.getUserId().getProfileImageUrl()) // Assuming getProfileImageUrl() method exists
                            .collect(Collectors.toList());

                    // Build and return OngoingProjectDto
                    return OngoingProjectDto.builder()
                            .teamId(teamMember.getTeamId().getTeamId())
                            .prjTitle(teamMember.getTeamId().getPrjTitle())
                            .profileImages(profileImages)
                            .build();
                })
                .collect(Collectors.toList());

        return ongoingProjectDtoList;
    }

    public List<OngoingProjectDto> getCompletedProjectsByUserId(Member member) {
        // Fetch the list of team members for the given user
        List<TeamMember> teamMemberList = teamMemberRepository.findAllByUserId(member);

        // Filter and map to OngoingProjectDto
        List<OngoingProjectDto> ongoingProjectDtoList = teamMemberList.stream()
                .filter(teamMember -> teamMember.getTeamId().getIsComplete() == true) // Correct filter condition for completed projects
                .map(teamMember -> {
                    // Fetch all team members for the team
                    List<TeamMember> teamMembers = teamMemberRepository.findAllByTeamId(teamMember.getTeamId());

                    // Collect profile images of team members
                    List<String> profileImages = teamMembers.stream()
                            .map(tm -> tm.getUserId().getProfileImageUrl()) // Assuming getProfileImageUrl() method exists
                            .collect(Collectors.toList());

                    // Build and return OngoingProjectDto
                    return OngoingProjectDto.builder()
                            .teamId(teamMember.getTeamId().getTeamId())
                            .prjTitle(teamMember.getTeamId().getPrjTitle())
                            .profileImages(profileImages)
                            .build();
                })
                .collect(Collectors.toList());

        return ongoingProjectDtoList;
    }


    private OngoingProjectDto convertToOngoingProjectDto(TeamPost teamPost) {
        List<String> profileImages = teamMemberRepository.findByTeamId_TeamId(teamPost.getTeamId()).stream()
                .map(teamMember -> {
                    Profile profile = profileRepository.findByUserId(teamMember.getUserId());
                    return profile != null ? profile.getProfileImageUrl() : null;
                })
                .collect(Collectors.toList());
        return new OngoingProjectDto(teamPost.getTeamId(), teamPost.getPrjTitle(), profileImages);
    }

    private ProjectDto convertToDto(Post post) {
        ProjectDto projectDto = new ProjectDto();
        projectDto.setPostId(post.getPostId());
        projectDto.setTitle(post.getTitle());
        projectDto.setRegionId(post.getRegion().getName());
        projectDto.setSubRegionId(post.getSubRegion().getName());
        projectDto.setOnoff(post.getOnOff().getKorean());
        projectDto.setStack(post.getStack());
        projectDto.setDifficulty(post.getDifficulty().getKorean());
        projectDto.setUserId(post.getUserId().getUserId());
        projectDto.setProjectPeriod(post.getProjectPeriod().getKorean());

        return projectDto;
    }
}

