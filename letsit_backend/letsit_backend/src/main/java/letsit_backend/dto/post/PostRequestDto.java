package letsit_backend.dto.post;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @NotNull(message = "모집 인원은 필수입니다.")
    private Post.TotalPersonnel totalPersonnel;

    @NotNull(message = "마감일은 필수입니다.")
    @Future(message = "마감일은 현재보다 이후여야 합니다.")
    private LocalDate recruitDueDate;

    @NotBlank(message = "우대사항은 필수입니다.")
    private String preference;

    @NotNull(message = "기술 스택은 필수입니다.")
    @Size(min = 1, message = "최소 한 개 이상의 기술 스택이 필요합니다.")
    private List<@NotBlank(message = "빈 스택 이름은 허용되지 않습니다.") String> stack;

    @NotNull(message = "소프트 스킬은 필수입니다.")
    @Size(min = 1, message = "최소 한 개 이상의 소프트 스킬이 필요합니다.")
    private List<@NotBlank(message = "빈 소프트 스킬 이름은 허용되지 않습니다.") String> softSkills;

    @NotNull(message = "난이도는 필수입니다.")
    private Post.Difficulty difficulty;

    @NotNull(message = "온/오프라인 여부는 필수입니다.")
    private Post.OnOff onOff;

    @NotNull(message = "지역 ID는 필수입니다.")
    private Long regionId;

    @NotNull(message = "하위 지역 ID는 필수입니다.")
    private Long subRegionId;

    @NotNull(message = "카테고리는 필수입니다.")
    @Size(min = 1, message = "최소 한 개 이상의 카테고리가 필요합니다.")
    private List<@NotBlank(message = "빈 카테고리 이름은 허용되지 않습니다.") String> categories;

    private LocalDate projectStartDate;

    @Future(message = "종료일은 현재보다 이후여야 합니다.")
    private LocalDate projectEndDate;

    @NotNull(message = "연령대는 필수입니다.")
    private Post.AgeGroup ageGroup;

    private Post.AgeGroupDetail ageGroupDetail;
}