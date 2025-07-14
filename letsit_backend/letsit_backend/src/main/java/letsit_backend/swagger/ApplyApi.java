package letsit_backend.swagger;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import letsit_backend.CurrentUser;
import letsit_backend.dto.Response;
import letsit_backend.dto.apply.ApplicantProfileDto;
import letsit_backend.dto.apply.ApplyRequestDto;
import letsit_backend.dto.apply.ApplyResponseDto;
import letsit_backend.model.Member;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


@Tag(name = "구인글 지원 관련 기능", description = "/Apply 로 시작하는 API들입니다.")
public interface ApplyApi {

    @Operation(summary = "지원서 제출", description = "구인글에 지원서를 작성하는 기능입니다.")
    @Parameter(name = "postId", description = "구인글 ID", example = "1")
    Response<ApplyResponseDto> postNewApply(@PathVariable("postId") Long postId, @CurrentUser Member member, @RequestBody ApplyRequestDto request);

    Response<ApplyResponseDto> getApply(@PathVariable("applyId") Long applyId, @CurrentUser Member member);

    Response<String> deleteApply(@PathVariable("applyId") Long applyId, @CurrentUser Member member);

    Response<List<ApplicantProfileDto>> getApplicantList(@PathVariable("postId") Long postId, @CurrentUser Member member);

    Response<List<ApplicantProfileDto>> getApprovedApplicantList(@PathVariable("postId") Long postId, @CurrentUser Member member);

    Response<String> approvalApplicant(@PathVariable("postId") Long postId, @PathVariable("applyId") Long applyId, @CurrentUser Member member);

    Response<String> rejectionApplicant(@PathVariable("postId") Long postId, @PathVariable("applyId") Long applyId, @CurrentUser Member member);

}
