package letsit_backend.service.post;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import letsit_backend.dto.post.PostRequestDto;
import letsit_backend.dto.post.PostResponseDto;
import letsit_backend.exception.CustomException;
import letsit_backend.model.Member;
import letsit_backend.model.Post;
import letsit_backend.model.Role;
import letsit_backend.repository.MemberRepository;
import letsit_backend.repository.PostRepository;
import letsit_backend.service.PostService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PostServiceTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    PostService postService;

    @BeforeEach
    public void before() {
        postRepository.deleteAll();
        memberRepository.deleteAll();
        System.out.println("Test Before");
    }

    @AfterEach
    public void after() {
        postRepository.deleteAll();
        memberRepository.deleteAll();
        System.out.println("Test After");
    }

    // 게시글 업로드
    @Test
    public void upload() throws Exception {
        // given
        Member member = createMember(1L, "테스트 유저");
        PostRequestDto requestDto = buildPostRequest();

        // when
        PostResponseDto response = postService.createPost(member, requestDto);

        // then
        assertThat(response.getTitle()).isEqualTo(requestDto.getTitle());
        assertThat(response.getContent()).isEqualTo(requestDto.getContent());
        assertThat(response.getTotalPersonnel()).isEqualTo(requestDto.getTotalPersonnel());
        assertThat(response.getRecruitDueDate()).isEqualTo(requestDto.getRecruitDueDate());
        assertThat(response.getSoftSkills()).contains("통솔력이 있어요");
        assertThat(response.getStack()).contains("java");
    }

    // 게시글 단건 조회
    @Test
    public void getOnePost() throws Exception {
        // given
        Member member = createMember(1L, "테스트 유저");
        PostRequestDto requestDto = buildPostRequest();
        PostResponseDto createdPost = postService.createPost(member, requestDto);

        // when
        PostResponseDto foundPost = postService.getPostById(createdPost.getPostId());

        // then
        assertThat(foundPost.getTitle()).isEqualTo(requestDto.getTitle());
        assertThat(foundPost.getContent()).isEqualTo(requestDto.getContent());
    }

    // 게시글 조회수 증가
    @Test
    public void getPost_shouldIncreaseViewCount() {
        // given
        Member member = createMember(1L, "테스트 유저");
        PostRequestDto requestDto = buildPostRequest();
        PostResponseDto createdPost = postService.createPost(member, requestDto);

        // when
        postService.getPostById(createdPost.getPostId());
        Post post = postRepository.findById(createdPost.getPostId()).orElseThrow();

        // then
        assertThat(post.getViewCount()).isEqualTo(1);
    }


    // 게시글 수정
    @Test
    public void updatePost() throws Exception {
        // given
        Member member = createMember(1L, "테스트 유저");
        PostRequestDto requestDto = buildPostRequest();
        PostResponseDto createdPost = postService.createPost(member, requestDto);

        // when
        requestDto.setTitle("수정된 제목");
        requestDto.setContent("수정된 내용");
        PostResponseDto updatedPost = postService.updatePost(member, createdPost.getPostId(), requestDto);

        // then
        assertThat(updatedPost.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedPost.getContent()).isEqualTo("수정된 내용");
    }

    // 게시글 삭제
    @Test
    public void deletePost() throws Exception {
        // given
        Member member = createMember(1L, "테스트 유저");
        PostRequestDto requestDto = buildPostRequest();
        PostResponseDto createdPost = postService.createPost(member, requestDto);

        // when
        postService.deletePost(member, createdPost.getPostId());

        // then
        assertThat(postRepository.findById(createdPost.getPostId())).isEmpty();
    }

    // 게시글 모집 마감
    @Test
    public void closePost() throws Exception {
        // given
        Member member = createMember(1L, "테스트 유저");
        PostRequestDto requestDto = buildPostRequest();
        PostResponseDto createdPost = postService.createPost(member, requestDto);

        // when
        postService.closePost(member, createdPost.getPostId());
        //postService.closePost(member, createdPost.getPostId());

        // then
        Post post = postRepository.findById(createdPost.getPostId()).orElseThrow();
        assertThat(post.getDeadline()).isTrue(); // 마감 여부 확인
    }

    // 게시글 모집 마감 실패 (작성자만 마감 가능)
    @Test
    public void closePostFail() throws Exception {
        // given
        Member member1 = createMember(1L, "테스트 유저 1");
        Member member2 = createMember(2L, "테스트 유저 2");
        PostRequestDto requestDto = buildPostRequest();
        PostResponseDto createdPost = postService.createPost(member1, requestDto);

        // when & then
        try {
            postService.closePost(member2, createdPost.getPostId());
        } catch (CustomException e) {
            assertThat(e.getMessage()).isEqualTo("게시글 작성자와 일치하지 않습니다.");
        }
    }

    // 게시글 모집 마감 실패 (이미 마감된 게시글)
    @Test
    public void closePostAlreadyClosed() throws Exception {
        // given
        Member member = createMember(1L, "테스트 유저");
        PostRequestDto requestDto = buildPostRequest();
        PostResponseDto createdPost = postService.createPost(member, requestDto);

        // 이미 마감 처리
        postService.closePost(member, createdPost.getPostId());

        // when & then
        try {
            postService.closePost(member, createdPost.getPostId());
        } catch (CustomException e) {
            assertThat(e.getMessage()).isEqualTo("이미 마감된 게시글입니다.");
        }
    }

    // 게시글 리스트 조회 (마감되지 않은 게시글만)
    @Test
    public void getRecruitingPosts() throws Exception {
        // given
        Member member1 = createMember(1L, "테스트 유저 1");
        Member member2 = createMember(2L, "테스트 유저 2");

        PostRequestDto requestDto1 = buildPostRequest();
        PostRequestDto requestDto2 = buildPostRequest();

        postService.createPost(member1, requestDto1);
        postService.createPost(member2, requestDto2);

        // when
        List<PostResponseDto> posts = postService.getRecruitingPostsByCreatedAt();

        // then
        assertThat(posts).extracting("title")
                .containsExactly(requestDto1.getTitle(), requestDto2.getTitle());

    }

    private Member createMember(Long kakaoId, String name) {
        Member member = Member.builder()
                .name(name)
                .role(Role.USER)
                .gender("여성")
                .kakaoId(kakaoId)
                .ageRange("20대")
                .kakaoAccessToken("testAccessToken")
                .profileImageUrl("testProfileImageUrl")
                .build();

        return memberRepository.save(member);
    }

    private PostRequestDto buildPostRequest() {
        PostRequestDto post = new PostRequestDto();
        post.setTitle("프로젝트 제목");
        post.setContent("프로젝트 내용");
        post.setTotalPersonnel(Post.TotalPersonnel.FIVE);
        post.setRecruitDueDate(LocalDate.parse("2025-07-31"));
        post.setProjectStartDate(LocalDate.parse("2025-08-01"));
        post.setProjectEndDate(LocalDate.parse("2025-08-31"));
        post.setDifficulty(Post.Difficulty.BASIC);
        post.setOnOff(Post.OnOff.ON);
        post.setRegionId(1L);
        post.setSubRegionId(1L);
        post.setPreference("인근 거주자 우대");
        post.setAgeGroup(Post.AgeGroup.S20);
        post.setAgeGroupDetail(Post.AgeGroupDetail.MID);
        post.setStack(List.of("java", "python"));
        post.setSoftSkills(List.of("통솔력이 있어요"));
        post.setCategories(List.of("데브옵스"));
        return post;
    }

}