//package letsit_backend.controller;
//
//import jakarta.validation.Valid;
//import letsit_backend.CurrentUser;
//import letsit_backend.dto.post.PostRequestDto;
//import letsit_backend.dto.post.PostResponseDto;
//import letsit_backend.dto.Response;
//import letsit_backend.model.Member;
//import letsit_backend.service.PostService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/posts")
//@RequiredArgsConstructor
//public class PostController {
//
//    private final PostService postService;
//
//    @ResponseStatus(HttpStatus.CREATED)
//    @PostMapping("/upload")
//    public Response<PostResponseDto> createPost(@Valid @RequestBody PostRequestDto requestDto, @CurrentUser Member member) {
//        if (member == null) {
//            return Response.fail("미인증 회원");
//        }
//        requestDto.setUserId(member.getUserId());
//        PostResponseDto responseDto = postService.createPost(requestDto);
//        return Response.success("구인 글이 성공적으로 등록되었습니다.", responseDto);
//    }
//
//    // 게시글 수정
//    @PutMapping("/{postId}/update")
//    public ResponseEntity<?> updatePost(@PathVariable("postId") Long postId, @RequestBody PostRequestDto postRequestDto) {
//        try {
//            PostResponseDto updatedPost = postService.updatePost(postId, postRequestDto);
//            return ResponseEntity.status(HttpStatus.CREATED).body(new Response<>(true, "구인 글이 성공적으로 수정되었습니다.", updatedPost));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>(false, "유효성 검사 오류", e.getMessage()));
//        }
//    }
//
//    @DeleteMapping("/delete/{postId}")
//    public ResponseEntity<Response<Void>> deletePost(@CurrentUser Member member, @PathVariable("postId") Long postId) {
//        if (member == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Response.fail("인증이 필요합니다. 로그인 후 다시 시도해 주세요."));
//        }
//        boolean isDeleted = postService.deletePost(member, postId);
//        if (isDeleted) {
//            return ResponseEntity.status(HttpStatus.OK).body(Response.success("게시글이 성공적으로 삭제되었습니다.", null));
//        } else {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Response.fail("인증이 필요합니다. 로그인 후 다시 시도해 주세요."));
//        }
//    }
//
//    @GetMapping("{postId}")
//    public ResponseEntity<?> getPostById(@PathVariable("postId") Long postId) {
//        try {
//            PostResponseDto postResponseDto = postService.getPostById(postId);
//            return ResponseEntity.status(HttpStatus.OK).body(postResponseDto);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>(false, "Invalid region parameter"));
//        }
//    }
//
//    @PostMapping("/{postId}/close")
//    @ResponseStatus(HttpStatus.OK)
//    public Response<String> closePost(@PathVariable Long postId) {
//        boolean isClosed = postService.closePost(postId);
//        if (isClosed) {
//            return new Response<>(true, "모집이 마감되었습니다.");
//        } else {
//            return new Response<>(false, "모집 마감에 실패했습니다.");
//        }
//    }
//
//    // 최신순으로 모든 게시글 조회
//    @GetMapping("/list")
//    public ResponseEntity<List<PostResponseDto>> getAllPosts() {
//        List<PostResponseDto> posts = postService.getAllPostsByDeadlineFalseOrderByCreatedAt();
//        return ResponseEntity.ok(posts);
//    }
//}
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

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/upload")
    public Response<PostResponseDto> createPost(@Valid @RequestBody PostRequestDto requestDto, @CurrentUser Member member) {
        if (member == null) {
            return Response.fail("미인증 회원");
        }
        requestDto.setUserId(member.getUserId());
        PostResponseDto responseDto = postService.createPost(requestDto);
        return Response.success("구인 글이 성공적으로 등록되었습니다.", responseDto);
    }

    // 게시글 수정
    @PutMapping("/{postId}/update")
    public Response<PostResponseDto> updatePost(@PathVariable Long postId, @RequestBody PostRequestDto postRequestDto) {
        try {
            PostResponseDto updatedPost = postService.updatePost(postId, postRequestDto);
            return Response.success("구인 글이 성공적으로 수정되었습니다.", updatedPost);
        } catch (IllegalArgumentException e) {
            return Response.fail("유효성 검사 오류: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{postId}")
    public Response<?> deletePost(@CurrentUser Member member, @PathVariable("postId") Long postId) {
        if (member == null) {
            return Response.fail("인증이 필요합니다. 로그인 후 다시 시도해 주세요.");
        }
        boolean isDeleted = postService.deletePost(member, postId);
        if (isDeleted) {
            return Response.success("게시글이 성공적으로 삭제되었습니다.", null);
        } else {
            return Response.fail("게시글 삭제 실패");
        }
    }

    @GetMapping("{postId}")
    public Response<PostResponseDto> getPostById(@PathVariable("postId") Long postId) {
        try {
            PostResponseDto postResponseDto = postService.getPostById(postId);
            return Response.success("조회 성공", postResponseDto);
        } catch (IllegalArgumentException e) {
            return Response.fail("Invalid region parameter");
        }
    }

    @PostMapping("/{postId}/close")
    @ResponseStatus(HttpStatus.OK)
    public Response<?> closePost(@PathVariable("postId") Long postId) {
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

