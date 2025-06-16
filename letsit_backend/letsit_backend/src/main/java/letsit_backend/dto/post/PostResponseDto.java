package letsit_backend.dto.post;

import letsit_backend.dto.comment.CommentResponseDto;
import letsit_backend.model.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostResponseDto {
    private Long userId;
    private Long postId;
    private String title;
    private String content;
    private Post.TotalPersonnel totalPersonnel;
    private LocalDate recruitDueDate;
    private String preference;
    private List<String> stack;
    private List<String> softSkills;
    private Post.Difficulty difficulty;
    private Post.OnOff onOff;
    private Boolean deadline;
    private List<String> categories;
    private Post.AgeGroup ageGroup;
    private String region;
    private String subRegion;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int viewCount;
    private int scrapCount;
    private Post.ProjectPeriod projectPeriod;
    private List<CommentResponseDto> comments;

    public PostResponseDto(Long userId,
                           Long postId,
                           String title,
                           String content,
                           Post.TotalPersonnel totalPersonnel,
                           LocalDate recruitDueDate,
                           String preference,
                           List<String> stack,
                           List<String> softSkills,
                           Post.Difficulty difficulty,
                           Post.OnOff onOff,
                           Boolean deadline,
                           List<String> categories,
                           Post.AgeGroup ageGroup,
                           String region,
                           String subRegion,
                           Timestamp createdAt,
                           Timestamp updatedAt,
                           int viewCount,
                           int scrapCount,
                           Post.ProjectPeriod projectPeriod,
                           List<CommentResponseDto> comments) {
        this.userId = userId;
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.totalPersonnel = totalPersonnel;
        this.recruitDueDate = recruitDueDate;
        this.preference = preference;
        this.stack = stack;
        this.softSkills = softSkills;
        this.difficulty = difficulty;
        this.onOff = onOff;
        this.deadline = deadline;
        this.categories = categories;
        this.ageGroup = ageGroup;
        this.region = region;
        this.subRegion = subRegion;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.viewCount = viewCount;
        this.scrapCount = scrapCount;
        this.projectPeriod = projectPeriod;
        this.comments = comments;
    }

    public static PostResponseDto from(Post post, List<CommentResponseDto> comments) {
        return new PostResponseDto(
                post.getMember().getUserId(),
                post.getPostId(),
                post.getTitle(),
                post.getContent(),
                post.getTotalPersonnel(),
                post.getRecruitDueDate(),
                post.getPreference(),
                post.getPostSkillStacks().stream()
                        .map(pss -> pss.getSkillStack().getStackName())
                        .toList(),
                post.getPostSoftSkills().stream()
                        .map(pss -> pss.getSoftSkill().getSoftSkillName())
                        .toList(),
                post.getDifficulty(),
                post.getOnOff(),
                post.getDeadline(),
                post.getPostCategories().stream()
                        .map(pc -> pc.getCategory().getCategoryName())
                        .toList(),
                post.getAgeGroup(),
                post.getRegion().getName(),
                post.getSubRegion().getName(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getViewCount(),
                post.getScrapCount(),
                post.getProjectPeriod(),
                comments
        );
    }

}