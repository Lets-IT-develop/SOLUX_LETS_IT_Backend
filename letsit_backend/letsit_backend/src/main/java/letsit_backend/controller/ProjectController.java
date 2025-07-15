package letsit_backend.controller;

import letsit_backend.CurrentUser;
import letsit_backend.dto.project.OngoingProjectDto;
import letsit_backend.dto.project.ProjectDto;
import letsit_backend.dto.Response;
import letsit_backend.model.Member;
import letsit_backend.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping("/organizinglist")
    public Response<List<ProjectDto>> getOrganizingList(@CurrentUser Member member) {
        List<ProjectDto> projects = projectService.getProjectsByUserId(member);
        return Response.success("구인 중인 프로젝트 목록", projects);
    }

    @GetMapping("/appliedlist")
    public Response<List<ProjectDto>> getAppliedList(@CurrentUser Member member) {
        List<ProjectDto> projects = projectService.getAppliedProjectsByUserId(member);
        return Response.success("신청한 프로젝트 목록", projects);
    }

    @GetMapping("/ongoinglist")
    public Response<List<OngoingProjectDto>> getOngoingList(@CurrentUser Member member) {
        List<OngoingProjectDto> ongoingProjects = projectService.getOngoingProjectsByUserId(member);
        return Response.success("진행 중인 프로젝트 목록", ongoingProjects);
    }

    @GetMapping("/completedlist")
    public Response<List<OngoingProjectDto>> getCompletedList(@CurrentUser Member member) {
        List<OngoingProjectDto> ongoingProjects = projectService.getCompletedProjectsByUserId(member);
        return Response.success("완료된 프로젝트 목록", ongoingProjects);
    }
}