package letsit_backend.dto.post;

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
    private Post.Difficulty difficulty;
    private Post.OnOff onOff;
    private Boolean deadline;
    private List<String> categoryId;
    private Post.AgeGroup ageGroup;
    private String region;
    private String subRegion;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int viewCount;
    private int scrapCount;
    private Post.ProjectPeriod projectPeriod;
    private List<CommentResponseDto> comments;

    public PostResponseDto(Long userId, Long postId, String title, String content, Post.TotalPersonnel totalPersonnel, LocalDate recruitDueDate, String preference, List<String> stack, Post.Difficulty difficulty, Post.OnOff onOff, Boolean deadline, List<String> categoryId, Post.AgeGroup ageGroup, String region, String subRegion, Timestamp createdAt, Timestamp updatedAt, int viewCount, int scrapCount, Post.ProjectPeriod projectPeriod, List<CommentResponseDto> comments) {
        this.userId = userId;
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.totalPersonnel = totalPersonnel;
        this.recruitDueDate = recruitDueDate;
        this.preference = preference;
        this.stack = stack;
        this.difficulty = difficulty;
        this.onOff = onOff;
        this.deadline = deadline;
        this.categoryId = categoryId;
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
}