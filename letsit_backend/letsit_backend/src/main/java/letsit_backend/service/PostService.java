package letsit_backend.service;

import letsit_backend.dto.comment.CommentResponseDto;
import letsit_backend.dto.post.PostRequestDto;
import letsit_backend.dto.post.PostResponseDto;
import letsit_backend.exception.CommonErrorCode;
import letsit_backend.exception.CustomException;
import letsit_backend.exception.PostErrorCode;
import letsit_backend.model.*;
import letsit_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final AreaRepository areaRepository;
    private final CommentRepository commentRepository;
    private final ProfileRepository profileRepository;
    private final SkillStackRepository skillStackRepository;
    private final SoftSkillRepository softSkillRepository;
    private final CategoryRepository categoryRepository;

    private static final String INVALID_STACK = "유효하지 않은 스택 이름이 포함되어 있습니다.";
    private static final String INVALID_SOFT_SKILL = "유효하지 않은 소프트 스킬 이름이 포함되어 있습니다.";
    private static final String INVALID_CATEGORY = "유효하지 않은 카테고리 이름이 포함되어 있습니다.";


    // 게시글 생성
    @Transactional
    public PostResponseDto createPost(Member member, PostRequestDto requestDto) {
        Area region = findAreaById(requestDto.getRegionId());
        Area subRegion = findAreaById(requestDto.getSubRegionId());

        Post post = Post.builder()
                .member(member)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .totalPersonnel(requestDto.getTotalPersonnel())
                .recruitDueDate(requestDto.getRecruitDueDate())
                .projectStartDate(requestDto.getProjectStartDate())
                .projectEndDate(requestDto.getProjectEndDate())
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
                .ageGroupDetail(requestDto.getAgeGroupDetail())
                .build();

        // 소프트 스킬, 기술 스택, 카테고리 연관관계 설정 -> 먼저 이름으로 값 찾아옴
        List<SkillStack> skillStacks = skillStackRepository.findAllByStackNameIn(requestDto.getStack());
        List<SoftSkill> softSkills = softSkillRepository.findAllBySoftSkillNameIn(requestDto.getSoftSkills());
        List<Category> categories = categoryRepository.findAllByCategoryNameIn(requestDto.getCategories());

        // 찾아온 값과 request 값 개수가 동일한지 확인(누락된 값 없는지 확인)
        if (skillStacks.size() != requestDto.getStack().size()) {
            throw new CustomException(PostErrorCode.INVALID_STACK);
        }
        if (softSkills.size() != requestDto.getSoftSkills().size()) {
            throw new CustomException(PostErrorCode.INVALID_SOFT_SKILL);
        }
        if (categories.size() != requestDto.getCategories().size()) {
            throw new CustomException(PostErrorCode.INVALID_CATEGORY);
        }

        // 값 동기화(추가)
        post.syncSkillStacksWith(skillStacks);
        post.syncSoftSkillsWith(softSkills);
        post.syncCategoriesWith(categories);

        Post savedPost = postRepository.save(post);

        return PostResponseDto.from(savedPost, List.of());
    }

    // 게시글 수정
    @Transactional
    public PostResponseDto updatePost(Member member, Long postId, PostRequestDto requestDto) {
        Post post = findPostById(postId);
        Area region = findAreaById(requestDto.getRegionId());
        Area subRegion = findAreaById(requestDto.getSubRegionId());

        if (post.isClosed()) {
            throw new CustomException(PostErrorCode.POST_CLOSED);
        }
        if (!post.getMember().getUserId().equals(member.getUserId())) {
            throw new CustomException(PostErrorCode.NOT_MATCHING_USER);
        }

        // 게시글 정보 수정
        post.updatePost(
                requestDto.getTitle(),
                requestDto.getContent(),
                requestDto.getTotalPersonnel(),
                requestDto.getRecruitDueDate(),
                requestDto.getProjectStartDate(),
                requestDto.getProjectEndDate(),
                requestDto.getDifficulty(),
                requestDto.getOnOff(),
                region,
                subRegion,
                requestDto.getPreference(),
                requestDto.getAgeGroup(),
                requestDto.getAgeGroupDetail()
        );

        // 소프트 스킬, 기술 스택, 카테고리 값 찾아옴
        List<SkillStack> skillStacks = skillStackRepository.findAllByStackNameIn(requestDto.getStack());
        List<SoftSkill> softSkills = softSkillRepository.findAllBySoftSkillNameIn(requestDto.getSoftSkills());
        List<Category> categories = categoryRepository.findAllByCategoryNameIn(requestDto.getCategories());

        // 찾아온 값과 request 값 개수가 동일한지 확인(누락된 값 없는지 확인)
        if (skillStacks.size() != requestDto.getStack().size()) {
            throw new CustomException(PostErrorCode.INVALID_STACK);
        }
        if (softSkills.size() != requestDto.getSoftSkills().size()) {
            throw new CustomException(PostErrorCode.INVALID_SOFT_SKILL);
        }
        if (categories.size() != requestDto.getCategories().size()) {
            throw new CustomException(PostErrorCode.INVALID_CATEGORY);
        }

        // 값 동기화(수정)
        post.syncSkillStacksWith(skillStacks);
        post.syncSoftSkillsWith(softSkills);
        post.syncCategoriesWith(categories);

        return PostResponseDto.from(post, List.of());
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Member member, Long postId) {
        Post post = findPostById(postId);
        if (!post.getMember().getUserId().equals(member.getUserId())) {
            throw new CustomException(PostErrorCode.NOT_MATCHING_USER);
        }
        postRepository.delete(post);
    }

    // 게시글 조회
    @Transactional
    public PostResponseDto getPostById(Long postId) {

        Post post = findPostById(postId);

        post.increaseViewCount();
        postRepository.save(post);

        List<CommentResponseDto> comments = findCommentByPost(post);
        return PostResponseDto.from(post, comments);
    }

    // 작성자에 의한 게시글 마감처리
    @Transactional
    public void closePost(Member member, Long postId) {
        Post post = findPostById(postId);

        if (!post.getMember().getUserId().equals(member.getUserId())) {
            throw new CustomException(PostErrorCode.NOT_MATCHING_USER);
        }

        if (post.isClosed()) {
            throw new CustomException(PostErrorCode.POST_ALREADY_CLOSED);
        }

        post.setClosed();
        postRepository.save(post);
    }

    // 게시글 리스트업 (마감되지 않은 것만)
    // 리스트업에 댓글 필요 없어 보여서 일단은 빈 리스트로 return, 댓글 필요하면 추후 수정
    @Transactional(readOnly = true)
    public List<PostResponseDto> getRecruitingPostsByCreatedAt() {
        List<Post> posts = postRepository.findAllByDeadlineFalseOrderByCreatedAtDesc();
        return posts.stream().map(post -> PostResponseDto.from(post, List.of())).collect(Collectors.toList());
    }

    // findByX
    private Area findAreaById(Long areaId) {
        return areaRepository.findById(areaId)
                .orElseThrow(() -> new CustomException(PostErrorCode.AREA_NOT_FOUND));
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(PostErrorCode.POSTS_NOT_FOUND));
    }

    // TODO 프로필 조회에서 N+1 문제 발생 -> 추후 개선
    private List<CommentResponseDto> findCommentByPost(Post post) {
        return commentRepository.findByPostId(post).stream()
                .map(comment -> {
                    Profile profile = profileRepository.findByMember(comment.getUserId());
                    CommentResponseDto dto = new CommentResponseDto(
                            comment,
                            profileRepository);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
