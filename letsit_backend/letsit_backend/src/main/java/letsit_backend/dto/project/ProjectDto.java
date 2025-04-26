package letsit_backend.dto.project;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
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