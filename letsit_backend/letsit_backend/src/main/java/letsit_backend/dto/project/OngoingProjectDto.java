package letsit_backend.dto.project;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OngoingProjectDto {
    private Long teamId;
    private String prjTitle;
    private List<String> profileImages;
}
