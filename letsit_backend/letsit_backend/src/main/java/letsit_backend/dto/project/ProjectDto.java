package letsit_backend.dto.project;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class ProjectDto {
    private Long postId;
    private String title;
    private String regionId;
    private String subRegionId;
    private String onoff;
    private List<String> stack;
    private String difficulty;
    private List<String> categories;
    private Long userId;
    private LocalDate projectStartDate;
    private LocalDate projectEndDate;
}