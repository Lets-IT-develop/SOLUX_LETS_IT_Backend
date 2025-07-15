package letsit_backend.dto.project;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class OngoingProjectDto {
    private Long teamId;
    private String prjTitle;
    private List<String> profileImages;
    private List<String> stack;
    private List<String> categories;
    private LocalDate projectStartDate;
    private LocalDate projectEndDate;
    private Long progress;
}
