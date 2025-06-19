package letsit_backend.team;

import letsit_backend.dto.team.TeamCreateRequestDto;
import letsit_backend.dto.team.TeamInfoResponseDto;
import letsit_backend.model.*;
import letsit_backend.repository.*;
import letsit_backend.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeamService 단위 테스트")
public class TeamServiceTest {

    @Mock private PostRepository postRepository;
    @Mock private TeamPostRepository teamPostRepository;
    @Mock private TeamMemberRepository teamMemberRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private ProfileRepository profileRepository;

    @InjectMocks
    private TeamService teamService;

    private Member member;
    private Post post;
    private TeamPost teamPost;
    private TeamMember teamMember;
    private Profile profile;


    @BeforeEach
    void setUp() {
        member = Member.builder()
                .userId(1L)
                .name("testUser")
                .build();

        post = Post.builder()
                .postId(1L)
                .userId(member)
                .title("테스트 게시물 제목")
                .content("테스트 게시물 입니다.")
                .build();

        teamPost = TeamPost.builder()
                .teamId(1L)
                .post(post)
                .prjTitle("테스트 팀")
                .build();

        teamMember = TeamMember.builder()
                .teamMemberId(1L)
                .teamId(teamPost)
                .userId(member)
                .teamMemberRole(TeamMember.Role.Team_Leader)
                .joinedAt(LocalDateTime.now())
                .build();

        profile = Profile.builder()
                .profileId(1L)
                .profileImageUrl("https://profileUrl.com")
                .build();
    }

    @Nested
    @DisplayName("팀 게시판 생성 테스트")
    class CreateTeamPostTest {

        @Test
        @DisplayName("성공: 게시글 작성자가 팀 게시판 생성")
        void createTeamPost_Success() {
            // given
            Long postId = 1L;
            TeamCreateRequestDto requestDto = new TeamCreateRequestDto("test TeamPost");

            given(postRepository.findById(postId)).willReturn(Optional.of(post));
            given(teamPostRepository.save(any(TeamPost.class))).willReturn(teamPost);
            given(teamMemberRepository.save(any(TeamMember.class))).willReturn(teamMember);

            // when
            assertThatCode(()-> teamService.createTeamPost(postId, member, requestDto))
                    .doesNotThrowAnyException();

            // then
            verify(postRepository).findById(postId);
            verify(teamPostRepository).save(any(TeamPost.class));
            verify(teamMemberRepository).save(any(TeamMember.class));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 게시글로 팀 생성 시도")
        void createTeamPost_PostNotFound() {
            // given
            Long nonExistentPostId = 999L;
            TeamCreateRequestDto requestDto = new TeamCreateRequestDto("test TeamPost");

            given(postRepository.findById(nonExistentPostId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(()-> teamService.createTeamPost(nonExistentPostId, member, requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("post not found");
        }

        @Test
        @DisplayName("실패: 게시글 작성자가 아닌 사용자가 팀 생성 시도")
        void createTeamPost_NotPostOwner() {
            // given
            Long postId = 1L;
            Member otherMember = Member.builder()
                    .userId(2L)
                    .name("otherUser")
                    .build();
            TeamCreateRequestDto requestDto = new TeamCreateRequestDto("test TeamPost");

            given(postRepository.findById(postId)).willReturn(Optional.of(post));

            // when & then
            assertThatThrownBy(()-> teamService.createTeamPost(postId, otherMember, requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("post owner permission denied");
        }
    }

    @Nested
    @DisplayName("팀 멤버 생성 테스트")
    class CreateTeamMemberTest {

        @Test
        @DisplayName("성공: 팀장이 새로운 멤버 승인")
        void createTeamMember_Success() {
            // given
            Long teamId = 1L;
            Long memberId = 2L;

            Member targetMember = Member.builder()
                    .userId(2L)
                    .name("newMember")
                    .build();

            given(teamPostRepository.findById(teamId)).willReturn(Optional.of(teamPost));
            given(teamMemberRepository.findByMemberAndTeamPost(member, teamPost)).willReturn(Optional.of(teamMember));
            given(memberRepository.findById(memberId)).willReturn(Optional.of(targetMember));

            // when
            assertThatCode(()-> teamService.createTeamMember(teamId, memberId, member))
                    .doesNotThrowAnyException();

            // then
            verify(teamMemberRepository).save(any(TeamMember.class));
        }

        // 팀 멤버 생성 - 존재하지 않는 유저

        @Test
        @DisplayName("실패: 팀장이 아닌 사용자가 멤버 승인 시도")
        void createTeamMember_NotLeader() {
            // given
            Long teamId = 1L;
            Long memberIdToAdd = 999L;

            Member nonLeaderMember = Member.builder()
                    .userId(2L)
                    .name("NonLeader")
                    .build();

            TeamMember regularTeamMember = TeamMember.builder()
                    .teamMemberId(2L)
                    .teamId(teamPost)
                    .userId(nonLeaderMember)
                    .teamMemberRole(TeamMember.Role.Team_Member)
                    .build();

            given(teamPostRepository.findById(teamId)).willReturn(Optional.of(teamPost));
            given(teamMemberRepository.findByMemberAndTeamPost(nonLeaderMember, teamPost)).willReturn(Optional.of(regularTeamMember));

            // when & then
            assertThatThrownBy(()-> teamService.createTeamMember(teamId, memberIdToAdd, nonLeaderMember))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("TeamPost Permission Denied");
        }
    }

    @Nested
    @DisplayName("팀 정보 조회 테스트")
    class GetTeamInfoTest {

        @Test
        @DisplayName("성공: 팀멤버가 팀정보 조회")
        void getTeamInfo_Success() {
            // given
            Long teamId = 1L;
            List<TeamMember> teamMembers = Arrays.asList(teamMember);

            given(teamPostRepository.findById(teamId)).willReturn(Optional.of(teamPost));
            given(teamMemberRepository.existsByMemberAndTeamPost(member, teamPost)).willReturn(true);
            given(teamMemberRepository.findAllByTeamPost(teamPost)).willReturn(teamMembers);
            given(profileRepository.findByUserId(member)).willReturn(profile);

            // when
            TeamInfoResponseDto result = teamService.getTeamInfo(teamId, member);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTeamName()).isEqualTo("테스트 팀");
            assertThat(result.getTeamMemberInfo()).hasSize(1);
            assertThat(result.getTeamMemberInfo().getFirst().getUserName()).isEqualTo("testUser");
            assertThat(result.getTeamMemberInfo().getFirst().getProfileImageUrl()).isEqualTo("https://profileUrl.com");
        }

        @Test
        @DisplayName("실패: 팀멤버가 아닌 사용자가 팀정보 조회 시도")
        void getTeamInfo_NotTeamMember() {
            // given
            Long teamId = 1L;

            Member outsider = Member.builder()
                    .userId(2L)
                    .name("outsider")
                    .build();

            given(teamPostRepository.findById(teamId)).willReturn(Optional.of(teamPost));
            given(teamMemberRepository.existsByMemberAndTeamPost(outsider, teamPost)).willReturn(false);

            // when & then
            assertThatThrownBy(()-> teamService.getTeamInfo(teamId, outsider))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("teamPost Permission Denied");
        }
    }

    @Nested
    @DisplayName("프로젝트 완료 상태 조회 테스트")
    class IsCompletedTest {

        @Test
        @DisplayName("성공: 완료되지 않은 프로젝트 상태 조회")
        void isCompleted_Success() {
            // given
            Long teamId = 1L;

            given(teamPostRepository.findById(teamId)).willReturn(Optional.of(teamPost));
            given(teamMemberRepository.existsByMemberAndTeamPost(member, teamPost)).willReturn(true);

            // when
            boolean result = teamService.isCompleted(teamId, member);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("실패: 팀멤버가 아닌 사용자가 프로젝트 상태 조회 시도")
        void isCompleted_NotTeamMember() {
            // given
            Long teamId = 1L;

            Member outsider = Member.builder()
                    .userId(2L)
                    .name("outsider")
                    .build();

            given(teamPostRepository.findById(teamId)).willReturn(Optional.of(teamPost));
            given(teamMemberRepository.existsByMemberAndTeamPost(outsider, teamPost)).willReturn(false);

            // when & then
            assertThatThrownBy(()-> teamService.isCompleted(teamId, outsider))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("teamPost Permission Denied");
        }
    }

    @Nested
    @DisplayName("프로젝트 완료 처리 테스트")
    class CompleteTeamPostTest {
        @Test
        @DisplayName("성공: 팀장이 프로젝트를 완료 처리")
        void completeTeamPost_Success() {
            // given
            Long teamId = 1L;

            given(teamPostRepository.findById(teamId)).willReturn(Optional.of(teamPost));
            given(teamMemberRepository.findByMemberAndTeamPost(member, teamPost)).willReturn(Optional.of(teamMember));

            // when
            assertThatCode(()-> teamService.completeTeamPost(teamId, member))
                    .doesNotThrowAnyException();

            // then
            assertThat(teamPost.getIsComplete()).isTrue();
        }

        @Test
        @DisplayName("실패: 팀장이 아닌 사용자가 프로젝트 완료 처리 시도")
        void completeTeamPost_NotTeamLeader() {
            // given
            Long teamId = 1L;

            Member nonLeaderMember = Member.builder()
                    .userId(2L)
                    .name("NonLeader")
                    .build();

            TeamMember regularTeamMember = TeamMember.builder()
                    .teamMemberId(2L)
                    .teamId(teamPost)
                    .userId(nonLeaderMember)
                    .teamMemberRole(TeamMember.Role.Team_Member)
                    .build();

            given(teamPostRepository.findById(teamId)).willReturn(Optional.of(teamPost));
            given(teamMemberRepository.findByMemberAndTeamPost(nonLeaderMember, teamPost)).willReturn(Optional.of(regularTeamMember));

            // when & then
            assertThatThrownBy(()-> teamService.completeTeamPost(teamId, nonLeaderMember))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("TeamPost Permission Denied");
        }
    }

    @Nested
    @DisplayName("팀장 나가기 테스트")
    class DeleteTeamLeaderTest {

        @Test
        @DisplayName("성공: 팀장 혼자 남은 경우 팀 해체")
        void deleteTeamLeader_TeamDisband() {
            // given
            Long teamId = 1L;
            List<TeamMember> singleMemberList = Arrays.asList(teamMember);

            given(teamPostRepository.findById(teamId)).willReturn(Optional.of(teamPost));
            given(teamMemberRepository.findByMemberAndTeamPost(member, teamPost)).willReturn(Optional.of(teamMember));
            given(teamMemberRepository.findAllByTeamPostWithLock(teamPost)).willReturn(singleMemberList);

            // when
            assertThatCode(()-> teamService.deleteTeamLeader(teamId, member))
                    .doesNotThrowAnyException();

            // then
            verify(teamMemberRepository).delete(teamMember);
            verify(teamPostRepository).delete(teamPost);
        }

        @Test
        @DisplayName("성공: 다른 팀원이 있는 경우 리더 변경")
        void deleteTeamLeader_TransferLeadership() {
            // given
            Long teamId = 1L;
            Member member2 = Member.builder()
                    .userId(2L)
                    .name("testUser2")
                    .build();

            TeamMember teamMember2 = TeamMember.builder()
                    .teamMemberId(2L)
                    .teamId(teamPost)
                    .userId(member2)
                    .teamMemberRole(TeamMember.Role.Team_Member)
                    .joinedAt(LocalDateTime.now().minusDays(1))
                    .build();

            List<TeamMember> multipleMemberList = Arrays.asList(teamMember, teamMember2);

            given(teamPostRepository.findById(teamId)).willReturn(Optional.of(teamPost));
            given(teamMemberRepository.findByMemberAndTeamPost(member, teamPost)).willReturn(Optional.of(teamMember));
            given(teamMemberRepository.findAllByTeamPostWithLock(teamPost)).willReturn(multipleMemberList);

            // when
            assertThatCode(()-> teamService.deleteTeamLeader(teamId, member))
                    .doesNotThrowAnyException();

            // then
            verify(teamMemberRepository).save(teamMember2);
            verify(teamMemberRepository).delete(teamMember);
            assertThat(teamMember2.getTeamMemberRole()).isEqualTo(TeamMember.Role.Team_Leader);
        }

        @Test
        @DisplayName("실패: 팀장이 아닌 사용자가 팀 나가기 시도")
        void deleteTeamLeader_NotTeamLeader() {
            // given
            Long teamId = 1L;

            Member member2 = Member.builder()
                    .userId(2L)
                    .name("testUser2")
                    .build();

            TeamMember teamMember2 = TeamMember.builder()
                    .teamMemberId(2L)
                    .teamId(teamPost)
                    .userId(member2)
                    .teamMemberRole(TeamMember.Role.Team_Member)
                    .joinedAt(LocalDateTime.now().minusDays(1))
                    .build();

            given(teamPostRepository.findById(teamId)).willReturn(Optional.of(teamPost));
            given(teamMemberRepository.findByMemberAndTeamPost(member2, teamPost)).willReturn(Optional.of(teamMember2));

            // when
            assertThatThrownBy(()-> teamService.deleteTeamLeader(teamId, member2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("TeamPost Permission Denied");
        }
    }

    @Nested
    @DisplayName("팀원 강퇴 테스트")
    class DeleteTeamMemberTest {
        @Test
        @DisplayName("성공: 팀장이 팀원을 강퇴")
        void deleteTeamMember_Success() {
            // given
            Long teamId = 1L;
            Long teamMemberId = 2L;

            Member targetMember = Member.builder()
                    .userId(2L)
                    .name("targetUser")
                    .build();

            TeamMember targetTeamMembmer = TeamMember.builder()
                    .teamMemberId(2L)
                    .teamId(teamPost)
                    .userId(targetMember)
                    .teamMemberRole(TeamMember.Role.Team_Member)
                    .build();

            given(teamPostRepository.findById(teamId)).willReturn(Optional.of(teamPost));
            given(teamMemberRepository.findByMemberAndTeamPost(member, teamPost)).willReturn(Optional.of(teamMember));
            given(teamMemberRepository.findById(teamMemberId)).willReturn(Optional.of(targetTeamMembmer));

            // when
            assertThatCode(()-> teamService.deleteTeamMember(teamId, teamMemberId, member))
                    .doesNotThrowAnyException();

            // then
            verify(teamMemberRepository).delete(targetTeamMembmer);
        }

        @Test
        @DisplayName("실패: 팀장이 아닌 사용자가 팀원 강퇴 시도")
        void deleteTeamMember_NotTeamLeader() {
            // given
            Long teamId = 1L;
            Long teamMemberId = 2L;

            Member member2 = Member.builder()
                    .userId(2L)
                    .name("member2")
                    .build();

            TeamMember notLeaderMember = TeamMember.builder()
                    .teamMemberId(2L)
                    .teamId(teamPost)
                    .userId(member2)
                    .teamMemberRole(TeamMember.Role.Team_Member)
                    .build();

            given(teamPostRepository.findById(teamId)).willReturn(Optional.of(teamPost));
            given(teamMemberRepository.findByMemberAndTeamPost(member2, teamPost)).willReturn(Optional.of(notLeaderMember));

            // when & then
            assertThatThrownBy(()-> teamService.deleteTeamMember(teamId, teamMemberId, member2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("TeamPost Permission Denied");
        }

        @Test
        @DisplayName("실패: 존재하지 않은 팀원을 강퇴 시도")
        void deleteTeamMember_TeamMemberNotFound() {
            // given
            Long teamId = 1L;
            Long teamMemberId = 999L;

            given(teamPostRepository.findById(teamId)).willReturn(Optional.of(teamPost));
            given(teamMemberRepository.findByMemberAndTeamPost(member, teamPost)).willReturn(Optional.of(teamMember));

            // when & then
            assertThatThrownBy(()-> teamService.deleteTeamMember(teamId, teamMemberId, member))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("teamMember not found");
        }

    }
}
