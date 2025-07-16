package letsit_backend.project;

import letsit_backend.dto.project.OngoingProjectDto;
import letsit_backend.dto.project.ProjectDto;
import letsit_backend.model.*;
import letsit_backend.repository.*;
import letsit_backend.service.ProjectService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * ProjectService의 핵심 비즈니스 메서드들을 테스트하기 위한 단위 테스트 클래스입니다.
 * 이 테스트는 Spring 없이 실행되며, 의존성은 Mockito를 통해 Mock 객체로 대체됩니다.
 */
@ExtendWith(MockitoExtension.class) // JUnit5에서 Mockito 기능을 사용 가능하게 함
@DisplayName("ProjectService 단위 테스트")
class ProjectServiceTest {

    // ===== Mock 의존성 주입 =====
    @Mock
    private PostRepository postRepository;
    @Mock
    private ApplyRepository applyRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private ProfileRepository profileRepository;

    // ProjectService에 위 mock 객체들이 주입됨
    @InjectMocks
    private ProjectService projectService;

    private Member member;
    private Post post;
    private Area region;
    private Area subRegion;

    @BeforeEach
    void setup() {
        // 테스트 공통 Member 객체 생성
        member = Member.builder()
                .userId(1L)
                .name("홍길동")
                .build();

        // 지역 정보
        region = new Area(1L, "서울특별시", null);
        subRegion = new Area(101L, "강남구", region);

        // 기본 Post 객체 설정 (모든 테스트에서 재사용)
        post = Post.builder()
                .postId(100L)
                .title("AI 프로젝트")
                .member(member)
                .viewCount(10)
                .onOff(Post.OnOff.ON)
                .difficulty(Post.Difficulty.NORMAL)
                .projectStartDate(LocalDate.now().minusDays(5))
                .projectEndDate(LocalDate.now().plusDays(5))
                .recruitDueDate(LocalDate.now().plusDays(10))
                .region(region)
                .subRegion(subRegion)
                .deadline(false)
                .postCategories(emptyList())
                .postSkillStacks(emptyList())
                .content("AI 프로젝트 내용")
                .totalPersonnel(5)
                .currentPersonnel(2)
                .build();
    }

    @Test
    @DisplayName("회원이 작성한 프로젝트 목록 조회")
    void getProjectsByUserId() {
        // given: 특정 member가 작성한 미마감(post.deadline = false) 게시글 1건이 있다고 가정
        when(postRepository.findByMemberAndDeadlineFalse(member)).thenReturn(List.of(post));

        // when: 서비스 메서드 호출
        List<ProjectDto> result = projectService.getProjectsByUserId(member);

        // then: 반환된 ProjectDto 리스트가 1개이며, 제목이 예상과 일치함
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("AI 프로젝트");
    }

    @Test
    @DisplayName("회원이 신청한 프로젝트 목록 조회")
    void getAppliedProjectsByUserId() {
        // given: 해당 member가 신청한 post 1건이 있다고 가정 (post는 마감되지 않은 상태)
        Apply apply = Apply.builder()
                .postId(post)
                .member(member)
                .applyContent("신청합니다")
                .build();

        when(applyRepository.findByUserId(member)).thenReturn(List.of(apply));

        // when: 서비스 메서드 호출
        List<ProjectDto> result = projectService.getAppliedProjectsByUserId(member);

        // then: 신청한 프로젝트 중 마감되지 않은 것만 반환되어야 함
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("AI 프로젝트");
    }

    @Test
    @DisplayName("회원이 참여 중인 프로젝트 목록 조회")
    void getOngoingProjectsByUserId() {
        // given: member가 참여 중인 TeamPost (isComplete = false)
        TeamPost teamPost = TeamPost.builder()
                .teamId(1L)
                .prjTitle("AI 팀 프로젝트")
                .isComplete(false) // 현재 진행 중
                .post(post)
                .build();

        TeamMember teamMember = TeamMember.builder()
                .teamPost(teamPost)
                .member(member)
                .build();

        // mocking: member가 팀멤버로 속해 있는 TeamPost, 그리고 팀 구성원 조회
        when(teamMemberRepository.findAllByUserId(member)).thenReturn(List.of(teamMember));
        when(teamMemberRepository.findByTeamId_TeamId(1L)).thenReturn(List.of(teamMember));

        // mocking: member의 프로필 이미지 존재
        when(profileRepository.findByMember(member)).thenReturn(
                Profile.builder().profileImageUrl("https://img.png").build()
        );

        // when
        List<OngoingProjectDto> result = projectService.getOngoingProjectsByUserId(member);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPrjTitle()).isEqualTo("AI 팀 프로젝트");
        assertThat(result.get(0).getProgress()).isBetween(1L, 99L); // 날짜 기준 진행률 계산됨
    }

    @Test
    @DisplayName("회원이 완료한 프로젝트 목록 조회")
    void getCompletedProjectsByUserId() {
        // given: 완료된 프로젝트 (isComplete = true)
        TeamPost teamPost = TeamPost.builder()
                .teamId(2L)
                .prjTitle("완료된 프로젝트")
                .isComplete(true)
                .post(post)
                .build();

        TeamMember teamMember = TeamMember.builder()
                .teamPost(teamPost)
                .member(member)
                .build();

        // mocking: 완료된 프로젝트 팀 멤버로 설정
        when(teamMemberRepository.findAllByUserId(member)).thenReturn(List.of(teamMember));
        when(teamMemberRepository.findByTeamId_TeamId(2L)).thenReturn(List.of(teamMember));
        when(profileRepository.findByMember(member)).thenReturn(null); // 프로필 없는 케이스

        // when
        List<OngoingProjectDto> result = projectService.getCompletedProjectsByUserId(member);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPrjTitle()).isEqualTo("완료된 프로젝트");
        assertThat(result.get(0).getProfileImages()).containsExactly((String) null); // null 허용
    }

    // ========== 실패 / 경계 케이스 추가 ==========
    @Test
    @DisplayName("작성한 프로젝트가 없는 경우 - 빈 목록 반환")
    void getProjectsByUserId_whenNoProjects() {
        when(postRepository.findByMemberAndDeadlineFalse(member)).thenReturn(List.of());

        List<ProjectDto> result = projectService.getProjectsByUserId(member);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("신청한 프로젝트가 모두 마감된 경우 - 필터링되어 빈 목록")
    void getAppliedProjectsByUserId_whenAllClosed() {
        // 마감 처리된 post
        post.setClosed(); // deadline = true

        Apply apply = Apply.builder()
                .postId(post)
                .member(member)
                .applyContent("신청")
                .build();

        when(applyRepository.findByUserId(member)).thenReturn(List.of(apply));

        List<ProjectDto> result = projectService.getAppliedProjectsByUserId(member);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("참여 중인 프로젝트가 없을 경우 - 빈 목록 반환")
    void getOngoingProjectsByUserId_whenNoOngoingTeam() {
        when(teamMemberRepository.findAllByUserId(member)).thenReturn(List.of());

        List<OngoingProjectDto> result = projectService.getOngoingProjectsByUserId(member);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("완료된 프로젝트가 없을 경우 - 빈 목록 반환")
    void getCompletedProjectsByUserId_whenNoCompletedTeam() {
        when(teamMemberRepository.findAllByUserId(member)).thenReturn(List.of());

        List<OngoingProjectDto> result = projectService.getCompletedProjectsByUserId(member);

        assertThat(result).isEmpty();
    }
}
