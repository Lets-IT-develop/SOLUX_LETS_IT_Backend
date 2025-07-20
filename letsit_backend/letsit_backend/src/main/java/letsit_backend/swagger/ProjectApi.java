package letsit_backend.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import letsit_backend.CurrentUser;
import letsit_backend.dto.Response;
import letsit_backend.dto.auth.CustomOAuth2User;
import letsit_backend.dto.project.OngoingProjectDto;
import letsit_backend.dto.project.ProjectDto;
import letsit_backend.model.Member;

import java.util.List;

@Tag(name = "프로젝트 조회 관련 기능", description = "/projects 로 시작하는 API들입니다.")
public interface ProjectApi {
    @Operation(
            summary = "작성한 프로젝트 목록 조회",
            description = "현재 로그인한 사용자가 작성한(팀장으로 등록된) 프로젝트 목록을 조회합니다."
    )
    Response<List<ProjectDto>> getOrganizingList(@Parameter(hidden = true) @CurrentUser CustomOAuth2User oAuth2User);

    @Operation(
            summary = "지원한 프로젝트 목록 조회",
            description = "현재 로그인한 사용자가 지원한 프로젝트 목록을 조회합니다."
    )
    Response<List<ProjectDto>> getAppliedList(@Parameter(hidden = true) @CurrentUser CustomOAuth2User oAuth2User);

    @Operation(
            summary = "진행 중인 프로젝트 목록 조회",
            description = "현재 로그인한 사용자가 참여 중인 프로젝트 목록을 조회합니다."
    )
    Response<List<OngoingProjectDto>> getOngoingList(@Parameter(hidden = true) @CurrentUser CustomOAuth2User oAuth2User);

    @Operation(
            summary = "완료된 프로젝트 목록 조회",
            description = "현재 로그인한 사용자가 완료한 프로젝트 목록을 조회합니다."
    )
    Response<List<OngoingProjectDto>> getCompletedList(@Parameter(hidden = true) @CurrentUser CustomOAuth2User oAuth2User);
}
