package letsit_backend.service;

import letsit_backend.dto.comment.CommentResponseDto;
import letsit_backend.dto.post.PostRequestDto;
import letsit_backend.dto.post.PostResponseDto;
import letsit_backend.model.Area;
import letsit_backend.model.Member;
import letsit_backend.model.Post;
import letsit_backend.model.Profile;
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

    public PostResponseDto createPost(PostRequestDto requestDto) {

        Member user = memberRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        Area region = areaRepository.findById(requestDto.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid region ID"));

        Area subRegion = areaRepository.findById(requestDto.getSubRegionId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid sub-region ID"));

        Post post = Post.builder()
                .userId(user)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .totalPersonnel(requestDto.getTotalPersonnel())
                .recruitDueDate(requestDto.getRecruitDueDate())
                .projectPeriod(requestDto.getProjectPeriod())
                .difficulty(requestDto.getDifficulty())
                .onOff(requestDto.getOnOff())
                .region(region)
                .subRegion(subRegion)
                .categoryId(String.join(",", requestDto.getCategoryId()))
                .viewCount(0)
                .scrapCount(0)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .deadline(false)
                .stack(String.join(",", requestDto.getStack()))
                .preference(requestDto.getPreference())
                .ageGroup(requestDto.getAgeGroup())
                .build();

        Post savedPost = postRepository.save(post);

        return new PostResponseDto(
                user.getUserId(),
                savedPost.getPostId(),
                savedPost.getTitle(),
                savedPost.getContent(),
                savedPost.getTotalPersonnel(),
                savedPost.getRecruitDueDate(),
                savedPost.getPreference(),
                savedPost.getStack(),
                savedPost.getDifficulty(),
                savedPost.getOnOff(),
                savedPost.getDeadline(),
                savedPost.getCategoryId(),
                savedPost.getAgeGroup(),
                savedPost.getRegion().getName(),
                savedPost.getSubRegion().getName(),
                savedPost.getCreatedAt(),
                savedPost.getUpdatedAt(),
                savedPost.getViewCount(),
                savedPost.getScrapCount(),
                savedPost.getProjectPeriod(),
                null
        );
    }

    public PostResponseDto updatePost(Long postId, PostRequestDto postRequestDto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid postId"));
        Member user = memberRepository.findById(postRequestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        Area region = areaRepository.findById(postRequestDto.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid region ID"));

        Area subRegion = areaRepository.findById(postRequestDto.getSubRegionId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid sub-region ID"));


        post.setTitle(postRequestDto.getTitle());
        post.setContent(postRequestDto.getContent());
        post.setTotalPersonnel(postRequestDto.getTotalPersonnel());
        post.setRecruitDueDate(postRequestDto.getRecruitDueDate());
        post.setPreference(postRequestDto.getPreference());
        post.setRegion(region);
        post.setSubRegion(subRegion);
        post.setProjectPeriod(postRequestDto.getProjectPeriod());
        post.setAgeGroup(postRequestDto.getAgeGroup());
        post.setStack(postRequestDto.getStack());
        post.setDifficulty(postRequestDto.getDifficulty());
        post.setOnOff(postRequestDto.getOnOff());
        post.setCategoryId(postRequestDto.getCategoryId());
        post.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        Post updatedPost = postRepository.save(post);

        return new PostResponseDto(
                user.getUserId(),
                updatedPost.getPostId(),
                updatedPost.getTitle(),
                updatedPost.getContent(),
                updatedPost.getTotalPersonnel(),
                updatedPost.getRecruitDueDate(),
                updatedPost.getPreference(),
                updatedPost.getStack(),
                updatedPost.getDifficulty(),
                updatedPost.getOnOff(),
                updatedPost.getDeadline(),
                updatedPost.getCategoryId(),
                updatedPost.getAgeGroup(),
                updatedPost.getRegion().getName(),
                updatedPost.getSubRegion().getName(),
                updatedPost.getCreatedAt(),
                updatedPost.getUpdatedAt(),
                updatedPost.getViewCount(),
                updatedPost.getScrapCount(),
                updatedPost.getProjectPeriod(),
                null
        );
    }

    public boolean deletePost(Member user, Long postId) {
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            if (post.getUserId().getUserId().equals(user.getUserId())) {
                postRepository.deleteById(postId);
                return true;
            }
        }
        return false;
    }

    public PostResponseDto getPostById(Long postId) {
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();

            // viewCount 증가
            post.setViewCount(post.getViewCount() + 1);
            postRepository.save(post);

            List<CommentResponseDto> comments = commentRepository.findByPostId(post).stream()
                    .map(comment -> {
                        Profile profile = profileRepository.findByMember(comment.getUserId());
                        CommentResponseDto dto = new CommentResponseDto(
                                comment,
                                profileRepository);
                        return dto;
                    })
                    .collect(Collectors.toList());

            return new PostResponseDto(
                    post.getUserId().getUserId(),
                    post.getPostId(),
                    post.getTitle(),
                    post.getContent(),
                    post.getTotalPersonnel(),
                    post.getRecruitDueDate(),
                    post.getPreference(),
                    post.getStack(),
                    post.getDifficulty(),
                    post.getOnOff(),
                    post.getDeadline(),
                    post.getCategoryId(),
                    post.getAgeGroup(),
                    post.getRegion().getName(),
                    post.getSubRegion().getName(),
                    post.getCreatedAt(),
                    post.getUpdatedAt(),
                    post.getViewCount(),
                    post.getScrapCount(),
                    post.getProjectPeriod(),
                    comments
            );
        } else {
            throw new IllegalArgumentException("Invalid post ID");
        }
    }

    @Transactional
    public boolean closePost(Long postId) {
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            post.setDeadline(true);
            postRepository.save(post);
            return true;
        } else {
            return false;
        }
    }

    // 게시글 조회 (마감되지 않은 것만)
    public List<PostResponseDto> getAllPostsByDeadlineFalseOrderByCreatedAt() {
        List<Post> posts = postRepository.findAllByDeadlineFalseOrderByCreatedAtDesc();
        return posts.stream().map(this::convertToResponseDto).collect(Collectors.toList());
    }

    private PostResponseDto convertToResponseDto(Post post) {
        List<CommentResponseDto> comments = commentRepository.findByPostId(post).stream()
                .map(comment -> new CommentResponseDto(
                        comment,
                        profileRepository
                ))
                .collect(Collectors.toList());

        return new PostResponseDto(
                post.getUserId().getUserId(),
                post.getPostId(),
                post.getTitle(),
                post.getContent(),
                post.getTotalPersonnel(),
                post.getRecruitDueDate(),
                post.getPreference(),
                post.getStack(),
                post.getDifficulty(),
                post.getOnOff(),
                post.getDeadline(),
                post.getCategoryId(),
                post.getAgeGroup(),
                post.getRegion().getName(),
                post.getSubRegion().getName(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getViewCount(),
                post.getScrapCount(),
                post.getProjectPeriod(),
                comments
        );
    }
}
