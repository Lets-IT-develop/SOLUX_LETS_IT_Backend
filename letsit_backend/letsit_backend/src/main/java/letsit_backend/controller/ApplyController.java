package letsit_backend.controller;

import letsit_backend.CurrentUser;
import letsit_backend.dto.apply.ApplicantProfileDto;
import letsit_backend.dto.apply.ApplyRequestDto;
import letsit_backend.dto.apply.ApplyResponseDto;
import letsit_backend.dto.Response;
import letsit_backend.model.Member;
import letsit_backend.service.ApplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/apply")
public class ApplyController {
    private final ApplyService applyService;

    @PostMapping(value = "/{postId}/write")
    @ResponseStatus(HttpStatus.CREATED)
    public Response<ApplyResponseDto> postNewApply(@PathVariable("postId") Long postId, @CurrentUser Member member, @RequestBody ApplyRequestDto request) {
        ApplyResponseDto submittedApply = applyService.create(postId, member, request);
        return Response.success("성공", submittedApply);
    }

    @GetMapping("/{applyId}")
    @ResponseStatus(HttpStatus.OK)
    public Response<ApplyResponseDto> getApply(@PathVariable("applyId") Long applyId, @CurrentUser Member member) {
        ApplyResponseDto apply = applyService.read(applyId, member);
        return Response.success("지원서 보기", apply);
    }


    @DeleteMapping("/{applyId}/delete")
    // TODO 204는 응답 데이터가 없어야 함. 프론트와 상의 후 응답 코드 결정할 것.
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Response<String> deleteApply(@PathVariable("applyId") Long applyId, @CurrentUser Member member) {
        if (member == null) {
            return Response.fail("미인증 회원");
        }
        applyService.delete(applyId, member);
        return Response.success("지원서를 삭제하였습니다", null);
    }

    @GetMapping(value = "/{postId}/list")
    @ResponseStatus(HttpStatus.OK)
    public Response<List<ApplicantProfileDto>> getApplicantList(@PathVariable("postId") Long postId, @CurrentUser Member member) {
        if (member == null) {
            return Response.fail("미인증 회원");
        }
        List<ApplicantProfileDto> applicant = applyService.getPendingApplicantProfiles(postId, member);
        return Response.success("지원자 리스트", applicant);
    }

    @GetMapping(value = "/{postId}/approvedlist")
    @ResponseStatus(HttpStatus.OK)
    public Response<List<ApplicantProfileDto>> getApprovedApplicantList(@PathVariable("postId") Long postId, @CurrentUser Member member) {
        List<ApplicantProfileDto> approved = applyService.getApprovedApplicantProfiles(postId, member);
        return Response.success("승인된 지원자 리스트", approved);
    }


    @PatchMapping(value = "/{postId}/list/{applyId}/approval")
    // TODO 204는 응답 데이터가 없어야 함. 프론트와 상의 후 응답 코드 결정할 것.
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Response<String> approvalApplicant(@PathVariable("postId") Long postId, @PathVariable("applyId") Long applyId, @CurrentUser Member member) {
        applyService.approveApplicant(postId, applyId, member);
        return Response.success("지원서가 승인되었습니다.", null);
    }

    @PatchMapping(value = "/{postId}/list/{applyId}/reject")
    // TODO 204는 응답 데이터가 없어야 함. 프론트와 상의 후 응답 코드 결정할 것.
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Response<String> rejectionApplicant(@PathVariable("postId") Long postId, @PathVariable("applyId") Long applyId, @CurrentUser Member member) {
        applyService.rejectApplicant(postId, applyId, member);
        return Response.success("지원서가 거절되었습니다.", null);
    }
}
