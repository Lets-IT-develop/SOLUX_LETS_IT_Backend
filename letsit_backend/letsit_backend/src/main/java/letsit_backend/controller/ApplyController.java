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
    public Response<ApplyResponseDto> postNewApply(@PathVariable("postId") Long postId, @CurrentUser Member member, @RequestBody ApplyRequestDto request) {
        ApplyResponseDto submittedApply = applyService.create(postId, member, request);
        return Response.success("성공", submittedApply);
    }

    @GetMapping("/{applyId}")
    public Response<ApplyResponseDto> getApply(@PathVariable("applyId") Long applyId, @CurrentUser Member member) {
        ApplyResponseDto apply = applyService.read(applyId, member);
        return Response.success("지원서 보기", apply);
    }


    @DeleteMapping("/{applyId}/delete")
    public Response<String> deleteApply(@PathVariable("applyId") Long applyId, @CurrentUser Member member) {
        if (member == null) {
            return Response.fail("미인증 회원");
        }
        applyService.delete(applyId, member);
        return Response.success("지원서를 삭제하였습니다", null);
    }

    @GetMapping(value = "/{postId}/list")
    public Response<List<ApplicantProfileDto>> getApplicantList(@PathVariable("postId") Long postId, @CurrentUser Member member) {
        if (member == null) {
            return Response.fail("미인증 회원");
        }
        List<ApplicantProfileDto> applicant = applyService.getPendingApplicantProfiles(postId, member);
        return Response.success("지원자 리스트", applicant);
    }

    @GetMapping(value = "/{postId}/approvedlist")
    public Response<List<ApplicantProfileDto>> getApprovedApplicantList(@PathVariable("postId") Long postId, @CurrentUser Member member) {
        List<ApplicantProfileDto> approved = applyService.getApprovedApplicantProfiles(postId, member);
        return Response.success("승인된 지원자 리스트", approved);
    }


    @GetMapping(value = "/{postId}/list/{applyId}/approval")
    public Response<String> approvalApplicant(@PathVariable("postId") Long postId, @PathVariable("applyId") Long applyId, @CurrentUser Member member) {
        applyService.approveApplicant(postId, applyId, member);
        // log.info("Approval request received for Post ID: {} and Apply ID: {}", postId, applyId);
        // log.info("Application approved successfully for Apply ID: {}", applyId);
        return Response.success("지원서가 승인되었습니다.", null);
    }

    @GetMapping(value = "/{postId}/list/{applyId}/reject")
    public ResponseEntity<String> rejectionApplicant(@PathVariable("postId") Long postId, @PathVariable("applyId") Long applyId, @CurrentUser Member member) {
        applyService.rejectApplicant(postId, applyId, member);
        // log.info("Approval request received for Post ID: {} and Apply ID: {}", postId, applyId);
        // log.info("Application approved successfully for Apply ID: {}", applyId);
        return ResponseEntity.status(HttpStatus.OK).body("지원서가 거절되었습니다.");
    }
}
