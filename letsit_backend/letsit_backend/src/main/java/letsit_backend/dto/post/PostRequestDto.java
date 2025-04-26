package letsit_backend.dto.post;

import letsit_backend.model.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostRequestDto {
    private Long userId;
    private String title;
    private String content;
    private Post.TotalPersonnel totalPersonnel;
    private LocalDate recruitDueDate;
    private String preference;
//    private ProjectInfo projectInfo;
    private List<String> stack;
    private Post.Difficulty difficulty;
    private Post.OnOff onOff;
    private Long regionId;
    private Long subRegionId;
    private List<String> categoryId;
    private Post.ProjectPeriod projectPeriod;
    private Post.AgeGroup ageGroup;

//    @Getter
//    @Setter
//    @NoArgsConstructor
//    public static class RecruitDueDate {
//        private Timestamp startDate;
//        private Timestamp endDate;
//    }

//    @Getter
//    @Setter
//    @NoArgsConstructor
//    public static class ProjectInfo {
//        private Post.ProjectPeriod projectPeriod;
//        private Post.AgeGroup ageGroup;
//    }


}