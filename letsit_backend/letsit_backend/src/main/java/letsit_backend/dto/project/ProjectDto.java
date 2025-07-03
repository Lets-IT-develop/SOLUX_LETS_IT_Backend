package letsit_backend.dto.project;

import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDto {
    private Long postId;
    private String title;
    private String regionId;
    private String subRegionId;
    private String onoff;
    private List<String> stack;
    private String difficulty;
    private Long userId;
    private String projectPeriod;
}