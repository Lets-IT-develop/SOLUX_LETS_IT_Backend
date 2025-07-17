package letsit_backend.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import letsit_backend.CurrentUser;
import letsit_backend.dto.Response;
import letsit_backend.dto.post.PostRequestDto;
import letsit_backend.dto.post.PostResponseDto;
import letsit_backend.model.Member;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "게시글 관련 기능", description = "/posts 로 시작하는 API들입니다.")
public interface PostApi {
    @Operation(summary = "게시글 업로드", description = "구인 글을 새로 등록합니다.")
    Response<PostResponseDto> createPost(@Parameter(hidden = true) @CurrentUser Member member, @Valid @RequestBody PostRequestDto requestDto);

    @Operation(summary = "게시글 수정", description = "기존 구인 글을 수정합니다.")
    Response<PostResponseDto> updatePost(
            @Parameter(hidden = true) @CurrentUser Member member,
            @Parameter(name = "postId", description = "조회할 게시글 ID", example = "1") @PathVariable Long postId,
            @Valid @RequestBody PostRequestDto requestDto);

    @Operation(summary = "게시글 삭제", description = "작성한 구인 글을 삭제합니다.")
    Response<?> deletePost(
            @Parameter(hidden = true) @CurrentUser Member member,
            @Parameter(name = "postId", description = "조회할 게시글 ID", example = "1") @PathVariable("postId") Long postId);

    @Operation(summary = "게시글 단건 조회", description = "게시글 ID로 단건 조회합니다.")
    Response<PostResponseDto> getPostById(@Parameter(name = "postId", description = "조회할 게시글 ID", example = "1") @PathVariable("postId") Long postId);

    @Operation(summary = "게시글 모집 마감", description = "작성한 게시글의 모집을 마감 처리합니다.")
    Response<?> closePost(
            @Parameter(hidden = true) @CurrentUser Member member,
            @Parameter(name = "postId", description = "조회할 게시글 ID", example = "1") @PathVariable("postId") Long postId);

    @Operation(summary = "모든 게시글 조회", description = "모든 모집 중인 게시글을 최신순으로 조회합니다.")
    Response<List<PostResponseDto>> getAllPosts();

}
