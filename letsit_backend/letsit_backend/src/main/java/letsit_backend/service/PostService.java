package letsit_backend.service;

import letsit_backend.dto.comment.CommentResponseDto;
import letsit_backend.dto.post.PostRequestDto;
import letsit_backend.dto.post.PostResponseDto;
import letsit_backend.model.*;
import letsit_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final AreaRepository areaRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final ProfileRepository profileRepository;
    private final SkillStackRepository skillStackRepository;
    private final SoftSkillRepository softSkillRepository;
    private final CategoryRepository categoryRepository;

    // TODO findBy~~ 로직 중복 -> 공통으로 뽑아낼 것 -> 완료

    // 게시글 생성
    public PostResponseDto createPost(PostRequestDto requestDto) {

        Member user = findMemberById(requestDto.getUserId());
        Area region = findAreaById(requestDto.getRegionId());
        Area subRegion = findAreaById(requestDto.getSubRegionId());

        // TODO 소프트스킬 값 설정 추가
        Post post = Post.builder()
                .member(user)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .totalPersonnel(requestDto.getTotalPersonnel())
                .recruitDueDate(requestDto.getRecruitDueDate())
                .projectPeriod(requestDto.getProjectPeriod())
                .difficulty(requestDto.getDifficulty())
                .onOff(requestDto.getOnOff())
                .region(region)
                .subRegion(subRegion)
                .viewCount(0)
                .scrapCount(0)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .deadline(false)
                .preference(requestDto.getPreference())
                .ageGroup(requestDto.getAgeGroup())
                .build();

        // 기술 스택, 소프트 스킬, 카테고리 값이 없을 경우 빈 리스트로 초기화
        List<String> stackNames = Optional.ofNullable(requestDto.getStack()).orElse(List.of());
        List<String> softSkillNames = Optional.ofNullable(requestDto.getSoftSkills()).orElse(List.of());
        List<String> categoryNames = Optional.ofNullable(requestDto.getCategories()).orElse(List.of());

        // 소프트 스킬, 기술 스택, 카테고리 연관관계 설정 -> 먼저 이름으로 값 찾아옴
        List<SkillStack> skillStacks = skillStackRepository.findAllByStackNameIn(stackNames);
        List<SoftSkill> softSkills = softSkillRepository.findAllBySoftSkillNameIn(softSkillNames);
        List<Category> categories = categoryRepository.findAllByCategoryNameIn(categoryNames);

        // 찾아온 값과 request 값 개수가 동일한지 확인(누락된 값 없는지 확인)
        if (skillStacks.size() != requestDto.getStack().size()) {
            throw new IllegalArgumentException("유효하지 않은 스택 이름이 포함되어 있습니다.");
        }
        if (softSkills.size() != requestDto.getSoftSkills().size()) {
            throw new IllegalArgumentException("유효하지 않은 소프트 스킬 이름이 포함되어 있습니다.");
        }
        if (categories.size() != requestDto.getCategories().size()) {
            throw new IllegalArgumentException("유효하지 않은 카테고리 이름이 포함되어 있습니다.");
        }

        // 값 동기화(추가)
        post.syncSkillStacksWith(skillStacks);
        post.syncSoftSkillsWith(softSkills);
        post.syncCategoriesWith(categories);

        Post savedPost = postRepository.save(post);

        return PostResponseDto.from(savedPost, List.of());
    }

    // 게시글 수정
    public PostResponseDto updatePost(Long postId, PostRequestDto requestDto) {
        Post post = findPostById(postId);
        // Member user = findMemberById(requestDto.getUserId()); // 안쓰는데 굳이?
        Area region = findAreaById(requestDto.getRegionId());
        Area subRegion = findAreaById(requestDto.getSubRegionId());

        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContent());
        post.setTotalPersonnel(requestDto.getTotalPersonnel());
        post.setRecruitDueDate(requestDto.getRecruitDueDate());
        post.setPreference(requestDto.getPreference());
        post.setRegion(region);
        post.setSubRegion(subRegion);
        post.setProjectPeriod(requestDto.getProjectPeriod());
        post.setAgeGroup(requestDto.getAgeGroup());
        post.setDifficulty(requestDto.getDifficulty());
        post.setOnOff(requestDto.getOnOff());
        post.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        // 소프트 스킬, 기술 스택, 카테고리 값 찾아옴
        List<SkillStack> skillStacks = skillStackRepository.findAllByStackNameIn(requestDto.getStack());
        List<SoftSkill> softSkills = softSkillRepository.findAllBySoftSkillNameIn(requestDto.getSoftSkills());
        List<Category> categories = categoryRepository.findAllByCategoryNameIn(requestDto.getCategories());

        // 찾아온 값과 request 값 개수가 동일한지 확인(누락된 값 없는지 확인)
        if (skillStacks.size() != requestDto.getStack().size()) {
            throw new IllegalArgumentException("유효하지 않은 스택 이름이 포함되어 있습니다.");
        }
        if (softSkills.size() != requestDto.getSoftSkills().size()) {
            throw new IllegalArgumentException("유효하지 않은 소프트 스킬 이름이 포함되어 있습니다.");
        }
        if (categories.size() != requestDto.getCategories().size()) {
            throw new IllegalArgumentException("유효하지 않은 카테고리 이름이 포함되어 있습니다.");
        }

        // 값 동기화(수정)
        post.syncSkillStacksWith(skillStacks);
        post.syncSoftSkillsWith(softSkills);
        post.syncCategoriesWith(categories);

        return PostResponseDto.from(post, List.of());
    }

    // 게시글 삭제
    public void deletePost(Member user, Long postId) {
        // TODO true/false 반환하지 말고 에러 던져서 처리
        Post post = findPostById(postId);
        if (!post.getMember().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }
        postRepository.delete(post);
    }

    // 게시글 조회
    public PostResponseDto getPostById(Long postId) {
        Post post = findPostById(postId);

        post.increaseViewCount();
        postRepository.save(post);

        List<CommentResponseDto> comments = findCommentByPost(post);
        return PostResponseDto.from(post, comments);
    }

    // 마감기한 지남 -> 마감처리
    @Transactional
    public void closePost(Member user, Long postId) {
        Post post = findPostById(postId);

        if (!post.getMember().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("작성자만 마감처리할 수 있습니다.");
        }

        if (post.isClosed()) {
            throw new IllegalArgumentException("이미 마감된 게시글입니다.");
        }

        post.setClosed();
        postRepository.save(post);
    }

    // 게시글 리스트업 (마감되지 않은 것만)
    // TODO 읽기 전용 트랜잭션
    // 리스트업에 댓글 필요 없어 보여서 일단은 빈 리스트로 return, 댓글 필요하면 추후 수정
    @Transactional(readOnly = true)
    public List<PostResponseDto> getRecruitingPostsByCreatedAt() {
        List<Post> posts = postRepository.findAllByDeadlineFalseOrderByCreatedAtDesc();
        return posts.stream().map(post -> PostResponseDto.from(post, List.of())).collect(Collectors.toList());
    }

    // findByX
    private Member findMemberById(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원 아이디와 일치하는 사용자가 없습니다."));
    }

    private Area findAreaById(Long areaId) {
        return areaRepository.findById(areaId)
                .orElseThrow(() -> new IllegalArgumentException("지역 아이디와 일치하는 지역이 없습니다."));
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 아이디와 일치하는 게시글이 없습니다."));
    }

    // TODO 프로필 조회에서 N+1 문제 발생 -> 추후 개선
    private List<CommentResponseDto> findCommentByPost(Post post) {
        return commentRepository.findByPostId(post).stream()
                .map(comment -> {
                    Profile profile = profileRepository.findByUserId(comment.getUserId());
                    CommentResponseDto dto = new CommentResponseDto(
                            comment,
                            profileRepository);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
