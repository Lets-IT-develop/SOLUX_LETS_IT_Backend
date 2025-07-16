package letsit_backend.service.post;

import letsit_backend.dto.comment.CommentResponseDto;
import letsit_backend.dto.post.PostRequestDto;
import letsit_backend.dto.post.PostResponseDto;
import letsit_backend.exception.CommonErrorCode;
import letsit_backend.exception.CustomException;
import letsit_backend.exception.PostErrorCode;
import letsit_backend.model.*;
import letsit_backend.repository.*;
import letsit_backend.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService 단위 테스트")
public class PostServiceTest {

    // Repository 들을 Mock 객체로 선언
    @Mock
    private PostRepository postRepository;
    @Mock
    private AreaRepository areaRepository;
    @Mock
    private SkillStackRepository skillStackRepository;
    @Mock
    private SoftSkillRepository softSkillRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ProfileRepository profileRepository;

    // 테스트 대상 클래스에 Mock 주입
    @InjectMocks
    private PostService postService;

    // 테스트 공통 객체들
    private Member member;
    private Area region;
    private Area subRegion;
    private SkillStack skill1;
    private SkillStack skill2;
    private SoftSkill softSkill;
    private Category category;
    private Post post;

    @BeforeEach
    void setup() {
        // 공통 테스트 데이터 초기화
        member = Member.builder().userId(1L).name("홍길동").role(Role.USER).build();
        region = new Area(1L, "서울특별시", null);
        subRegion = new Area(101L, "강남구", region);
        skill1 = new SkillStack("java");
        skill2 = new SkillStack("python");
        softSkill = new SoftSkill("통솔력이 있어요", SoftSkill.SoftSkillSection.LEADERSHIP);
        category = new Category("백엔드 개발");

        // 게시글 객체 구성 (연관 엔티티 포함)
        post = Post.builder()
                .postId(1L)
                .member(member)
                .title("제목")
                .region(region)
                .subRegion(subRegion)
                .viewCount(0)
                .scrapCount(0)
                .deadline(false)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .difficulty(Post.Difficulty.NORMAL)
                .onOff(Post.OnOff.ON)
                .content("내용")
                .totalPersonnel(4)
                .currentPersonnel(0)
                .recruitDueDate(LocalDate.now().plusDays(10))
                .projectStartDate(LocalDate.now().plusDays(20))
                .projectEndDate(LocalDate.now().plusDays(50))
                .preference("상관 없음")
                .ageGroup(Post.AgeGroup.S20)
                .ageGroupDetail(Post.AgeGroupDetail.MID)
                .postCategories(List.of(new PostCategory(post, category)))
                .postSoftSkills(List.of(new PostSoftSkill(post, softSkill)))
                .postSkillStacks(List.of(new PostSkillStack(post, skill1), new PostSkillStack(post, skill2)))
                .build();
    }

    // 게시글 생성 성공 테스트
    @Test
    @DisplayName("게시글 생성 성공")
    void createPost_success() {
        PostRequestDto requestDto = buildPostRequestDto();

        // 정상 데이터 반환 설정
        given(areaRepository.findById(1L)).willReturn(Optional.of(region));
        given(areaRepository.findById(101L)).willReturn(Optional.of(subRegion));
        given(skillStackRepository.findAllByStackNameIn(List.of("java", "python"))).willReturn(List.of(skill1, skill2));
        given(softSkillRepository.findAllBySoftSkillNameIn(List.of("통솔력이 있어요"))).willReturn(List.of(softSkill));
        given(categoryRepository.findAllByCategoryNameIn(List.of("백엔드 개발"))).willReturn(List.of(category));
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        PostResponseDto response = postService.createPost(member, requestDto);

        assertThat(response.getTitle()).isEqualTo("프로젝트 제목");
        assertThat(response.getStack()).containsExactly("java", "python");
        assertThat(response.getSoftSkills()).containsExactly("통솔력이 있어요");
    }

    // 실패 케이스: 지역 없음
    @Test
    @DisplayName("게시글 생성 실패 - 존재하지 않는 지역")
    void createPost_fail_invalidRegion() {
        PostRequestDto requestDto = buildPostRequestDto();
        given(areaRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.createPost(member, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("지역 아이디와 일치하는 지역이 없습니다.");
    }

    // 실패 케이스: 기술 스택 매칭 실패
    @Test
    @DisplayName("게시글 생성 실패 - 유효하지 않은 기술스택")
    void createPost_fail_invalidSkill() {
        PostRequestDto requestDto = buildPostRequestDto();

        given(areaRepository.findById(1L)).willReturn(Optional.of(region));
        given(areaRepository.findById(101L)).willReturn(Optional.of(subRegion));
        given(skillStackRepository.findAllByStackNameIn(List.of("java", "python"))).willReturn(List.of());

        assertThatThrownBy(() -> postService.createPost(member, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("유효하지 않은 스택 이름이 포함되어 있습니다.");
    }

    // 실패 케이스: 게시글 작성자 불일치
    @Test
    @DisplayName("게시글 수정 실패 - 작성자 불일치")
    void updatePost_fail_unauthorizedUser() {
        Member otherUser = Member.builder().userId(2L).name("임꺽정").build();
        PostRequestDto requestDto = buildPostRequestDto();

        // 게시글 및 지역 mock 설정
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(areaRepository.findById(1L)).willReturn(Optional.of(region));
        given(areaRepository.findById(101L)).willReturn(Optional.of(subRegion));

        assertThatThrownBy(() -> postService.updatePost(otherUser, 1L, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("게시글 작성자와 일치하지 않습니다.");
    }

    // 실패 케이스: 마감된 게시글 수정 시도
    @Test
    @DisplayName("게시글 수정 실패 - 마감된 게시글")
    void updatePost_fail_postClosed() {
        // given
        post.setClosed(); // 게시글 마감 상태로 설정
        PostRequestDto requestDto = buildPostRequestDto();

        // 게시글 및 지역 mock 설정
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(areaRepository.findById(1L)).willReturn(Optional.of(region));
        given(areaRepository.findById(101L)).willReturn(Optional.of(subRegion));

        // then
        assertThatThrownBy(() -> postService.updatePost(member, 1L, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("마감된 게시글은 수정할 수 없습니다.");
    }

    // 게시글 수정 성공 테스트
    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost_success() {
        PostRequestDto requestDto = buildPostRequestDto();
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(areaRepository.findById(1L)).willReturn(Optional.of(region));
        given(areaRepository.findById(101L)).willReturn(Optional.of(subRegion));
        given(skillStackRepository.findAllByStackNameIn(List.of("java", "python"))).willReturn(List.of(skill1, skill2));
        given(softSkillRepository.findAllBySoftSkillNameIn(List.of("통솔력이 있어요"))).willReturn(List.of(softSkill));
        given(categoryRepository.findAllByCategoryNameIn(List.of("백엔드 개발"))).willReturn(List.of(category));

        PostResponseDto response = postService.updatePost(member, 1L, requestDto);

        assertThat(response.getTitle()).isEqualTo("프로젝트 제목");
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePost_success() {
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        doNothing().when(postRepository).delete(post);

        postService.deletePost(member, 1L);
        verify(postRepository).delete(post);
    }

    // 실패 케이스: 삭제 권한 없음
    @Test
    @DisplayName("게시글 삭제 실패 - 작성자 아님")
    void deletePost_fail_unauthorized() {
        Member other = Member.builder().userId(99L).build();
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.deletePost(other, 1L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("게시글 작성자와 일치하지 않습니다.");
    }

    @Test
    @DisplayName("게시글 조회 성공")
    void getPostById_success() {
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(commentRepository.findByPostId(post)).willReturn(List.of());
        given(postRepository.save(post)).willReturn(post);

        PostResponseDto response = postService.getPostById(1L);
        assertThat(response.getTitle()).isEqualTo("제목");
    }

    // 마감 처리 성공 테스트
    @Test
    @DisplayName("게시글 마감 성공")
    void closePost_success() {
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        postService.closePost(member, 1L);
        assertThat(post.isClosed()).isTrue();
    }

    // 마감 실패 테스트: 작성자 불일치
    @Test
    @DisplayName("게시글 마감 실패 - 작성자 아님")
    void closePost_fail_unauthorized() {
        Member other = Member.builder().userId(2L).build();
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.closePost(other, 1L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("게시글 작성자와 일치하지 않습니다.");
    }

    // 마감 실패 테스트: 이미 마감됨
    @Test
    @DisplayName("게시글 마감 실패 - 이미 마감됨")
    void closePost_fail_alreadyClosed() {
        post.setClosed();
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.closePost(member, 1L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 마감된 게시글입니다");
    }

    @Test
    @DisplayName("게시글 리스트 조회 성공")
    void getRecruitingPostsByCreatedAt_success() {
        given(postRepository.findAllByDeadlineFalseOrderByCreatedAtDesc()).willReturn(List.of(post));
        List<PostResponseDto> result = postService.getRecruitingPostsByCreatedAt();
        assertThat(result).hasSize(1);
    }

    // 테스트용 요청 DTO 빌더 메서드
    private PostRequestDto buildPostRequestDto() {
        return PostRequestDto.builder()
                .title("프로젝트 제목")
                .content("프로젝트 내용")
                .totalPersonnel(4)
                .recruitDueDate(LocalDate.now().plusDays(10))
                .projectStartDate(LocalDate.now().plusDays(20))
                .projectEndDate(LocalDate.now().plusDays(50))
                .difficulty(Post.Difficulty.NORMAL)
                .onOff(Post.OnOff.ON)
                .regionId(1L)
                .subRegionId(101L)
                .preference("상관 없음")
                .ageGroup(Post.AgeGroup.S20)
                .ageGroupDetail(Post.AgeGroupDetail.MID)
                .stack(List.of("java", "python"))
                .softSkills(List.of("통솔력이 있어요"))
                .categories(List.of("백엔드 개발"))
                .build();
    }
}
