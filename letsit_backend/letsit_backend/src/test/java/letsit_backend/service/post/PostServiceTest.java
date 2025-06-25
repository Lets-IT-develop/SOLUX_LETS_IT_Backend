package letsit_backend.service.post;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import letsit_backend.dto.post.PostRequestDto;
import letsit_backend.dto.post.PostResponseDto;
import letsit_backend.model.Member;
import letsit_backend.model.Post;
import letsit_backend.model.Role;
import letsit_backend.repository.MemberRepository;
import letsit_backend.repository.PostRepository;
import letsit_backend.service.PostService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

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
        System.out.println("Test Before");
    }

    @AfterEach
    public void after() {
        postRepository.deleteAll();
        memberRepository.deleteAll();
        System.out.println("Test After");
    }

    @Test
    public void upload() throws Exception {
        // given
        Member member = createMember(1L, "테스트 유저");
        PostRequestDto requestDto = buildPostRequest(member.getUserId());

        // when
        PostResponseDto response = postService.createPost(requestDto);

        // then
        assertThat(response.getTitle()).isEqualTo(requestDto.getTitle());
        assertThat(response.getContent()).isEqualTo(requestDto.getContent());
        assertThat(response.getTotalPersonnel()).isEqualTo(requestDto.getTotalPersonnel());
        assertThat(response.getRecruitDueDate()).isEqualTo(requestDto.getRecruitDueDate());
        assertThat(response.getSoftSkills()).contains("통솔력이 있어요");
        assertThat(response.getStack()).contains("java");
    }

    @Test
    public void getOnePost() throws Exception {
        // given
        Member member = createMember(1L, "테스트 유저");
        PostRequestDto requestDto = buildPostRequest(member.getUserId());
        PostResponseDto createdPost = postService.createPost(requestDto);

        // when
        PostResponseDto foundPost = postService.getPostById(createdPost.getPostId());

        // then
        assertThat(foundPost.getTitle()).isEqualTo(requestDto.getTitle());
        assertThat(foundPost.getContent()).isEqualTo(requestDto.getContent());
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

    private PostRequestDto buildPostRequest(Long userId) {
        PostRequestDto post = new PostRequestDto();
        post.setUserId(userId);
        post.setTitle("프로젝트 제목");
        post.setContent("프로젝트 내용");
        post.setTotalPersonnel(Post.TotalPersonnel.FIVE);
        post.setRecruitDueDate(LocalDate.parse("2025-07-31"));
        post.setProjectPeriod(Post.ProjectPeriod.ONE_MONTH);
        post.setDifficulty(Post.Difficulty.BASIC);
        post.setOnOff(Post.OnOff.ON);
        post.setRegionId(1L);
        post.setSubRegionId(1L);
        post.setPreference("인근 거주자 우대");
        post.setAgeGroup(Post.AgeGroup.S20A);
        post.setStack(List.of("java", "pyhton"));
        post.setSoftSkills(List.of("통솔력이 있어요"));
        post.setCategories(List.of("데브옵스"));
        return post;
    }

}