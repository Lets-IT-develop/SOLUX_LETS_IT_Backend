package letsit_backend.controller;

import jakarta.validation.Valid;
import letsit_backend.CurrentUser;
import letsit_backend.dto.auth.CustomOAuth2User;
import letsit_backend.dto.post.PostRequestDto;
import letsit_backend.dto.post.PostResponseDto;
import letsit_backend.dto.Response;
import letsit_backend.model.Member;
import letsit_backend.service.PostService;
import letsit_backend.swagger.PostApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController implements PostApi {

    private final PostService postService;

    // 게시글 업로드
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/upload")
    public Response<PostResponseDto> createPost(@CurrentUser CustomOAuth2User oAuth2User, @Valid @RequestBody PostRequestDto requestDto) {
        PostResponseDto responseDto = postService.createPost(oAuth2User, requestDto);
        return Response.success("구인 글이 성공적으로 등록되었습니다.", responseDto);
    }

    // 게시글 수정
    @PutMapping("/{postId}/update")
    public Response<PostResponseDto> updatePost(@CurrentUser CustomOAuth2User oAuth2User, @PathVariable Long postId, @Valid @RequestBody PostRequestDto requestDto) {
        PostResponseDto updatedPost = postService.updatePost(oAuth2User, postId, requestDto);
        return Response.success("구인 글이 성공적으로 수정되었습니다.", updatedPost);
    }

    // 게시글 삭제
    @DeleteMapping("/delete/{postId}")
    public Response<?> deletePost(@CurrentUser CustomOAuth2User oAuth2User, @PathVariable("postId") Long postId) {
        postService.deletePost(oAuth2User, postId);
        return Response.success("게시글이 성공적으로 삭제되었습니다.", null);
    }

    // 게시글 조회
    @GetMapping("{postId}")
    public Response<PostResponseDto> getPostById(@PathVariable("postId") Long postId) {
        PostResponseDto postResponseDto = postService.getPostById(postId);
        return Response.success("조회 성공", postResponseDto);
    }

    // 모집 마감 처리
    @PostMapping("/{postId}/close")
    @ResponseStatus(HttpStatus.OK)
    public Response<?> closePost(@CurrentUser CustomOAuth2User oAuth2User, @PathVariable("postId") Long postId) {
        postService.closePost(oAuth2User, postId);
        return Response.success("모집이 마감되었습니다.", null);
    }

    // 최신순으로 모든 게시글 조회
    @GetMapping("/list")
    public Response<List<PostResponseDto>> getAllPosts() {
        List<PostResponseDto> posts = postService.getRecruitingPostsByCreatedAt();
        return Response.success("모든 게시글 조회 성공", posts);
    }
}

