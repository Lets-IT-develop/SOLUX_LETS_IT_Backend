package letsit_backend.controller;

import jakarta.validation.Valid;
import letsit_backend.CurrentUser;
import letsit_backend.dto.post.PostRequestDto;
import letsit_backend.dto.post.PostResponseDto;
import letsit_backend.dto.Response;
import letsit_backend.model.Member;
import letsit_backend.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TODO fail 대신 커스텀 에러 날리기
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 업로드
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/upload")
    public Response<PostResponseDto> createPost(@Valid @RequestBody PostRequestDto requestDto, @CurrentUser Member member) {
        // TODO == null 로 체크하기 보다는 스프링 시큐리티의 인증 필터나 @ControllerAdvice 로 일괄 처리
        if (member == null) {
            return Response.fail("미인증 회원");
        }
        // TODO DTO setter 닫고 createPost에 넘겨주도록 수정 -> createPost에서 builder로 값 넣어주기
        requestDto.setUserId(member.getUserId());
        PostResponseDto responseDto = postService.createPost(requestDto);
        return Response.success("구인 글이 성공적으로 등록되었습니다.", responseDto);
    }

    // 게시글 수정
    // TODO @Valid 누락 수정
    @PutMapping("/{postId}/update")
    public Response<PostResponseDto> updatePost(@PathVariable Long postId, @RequestBody PostRequestDto postRequestDto) {
        // TODO try-catch 제거 -> service 로직에 위임
        try {
            PostResponseDto updatedPost = postService.updatePost(postId, postRequestDto);
            return Response.success("구인 글이 성공적으로 수정되었습니다.", updatedPost);
        } catch (IllegalArgumentException e) {
            return Response.fail("유효성 검사 오류: " + e.getMessage());
        }
    }

    // 게시글 삭제
    @DeleteMapping("/delete/{postId}")
    public Response<?> deletePost(@CurrentUser Member member, @PathVariable("postId") Long postId) {
        // TODO 업로드 부분 참고
        if (member == null) {
            return Response.fail("인증이 필요합니다. 로그인 후 다시 시도해 주세요.");
        }
        boolean isDeleted = postService.deletePost(member, postId);
        // TODO 파라미터 검증, 호출 제외한 로직은 service로 넘김 -> 그럼 반환은 어떻게?
        if (isDeleted) {
            return Response.success("게시글이 성공적으로 삭제되었습니다.", null);
        } else {
            return Response.fail("게시글 삭제 실패");
        }
    }

    // 게시글 조회
    @GetMapping("{postId}")
    public Response<PostResponseDto> getPostById(@PathVariable("postId") Long postId) {
        // TODO 파라미터 검증, 호출 제외한 로직은 service로 넘김
        try {
            PostResponseDto postResponseDto = postService.getPostById(postId);
            return Response.success("조회 성공", postResponseDto);
        } catch (IllegalArgumentException e) {
            return Response.fail("Invalid region parameter");
        }
    }

    // 모집 마감 처리
    @PostMapping("/{postId}/close")
    @ResponseStatus(HttpStatus.OK)
    public Response<?> closePost(@PathVariable("postId") Long postId) {
        // TODO 파라미터 검증, 호출 제외한 로직은 service로 넘김
        boolean isClosed = postService.closePost(postId);
        if (isClosed) {
            return Response.success("모집이 마감되었습니다.", postService.closePost(postId));
        } else {
            return Response.fail("모집 마감에 실패했습니다.");
        }
    }

    // 최신순으로 모든 게시글 조회
    @GetMapping("/list")
    public Response<List<PostResponseDto>> getAllPosts() {
        List<PostResponseDto> posts = postService.getAllPostsByDeadlineFalseOrderByCreatedAt();
        return Response.success("모든 게시글 조회 성공", posts);
    }
}

