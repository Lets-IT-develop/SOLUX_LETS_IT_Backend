package letsit_backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import letsit_backend.dto.post.KoreanEnum;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private Member userId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    private TotalPersonnel totalPersonnel;

    @ColumnDefault("0")
    private int currentPersonnel;

    @Column(nullable = false)
    private LocalDate recruitDueDate;

    @Enumerated(EnumType.STRING)
    private ProjectPeriod projectPeriod;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    private OnOff onOff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Area region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_region_id")
    private Area subRegion;

    @OneToMany(mappedBy = "post",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PostCategory> postCategories = new ArrayList<>();

    private int viewCount;

    private int scrapCount;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @Column(nullable = false)
    private Boolean deadline;

    @Column(name = "stack")
    private String stack;

    private String preference;

    @OneToMany(mappedBy = "post",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PostSoftSkill> postSoftSkills = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgeGroup ageGroup;

    // ========= Enum =========

    // 인원
    @AllArgsConstructor
    @Getter
    public enum TotalPersonnel implements KoreanEnum {
        TWO("2명", 2),
        THREE("3명", 3),
        FOUR("4명", 4),
        FIVE("5명", 5),
        SIX("6명", 6),
        SEVEN("7명", 7),
        EIGHT("8명", 8);

        @JsonValue
        private final String korean;
        private final int value;

        @JsonCreator
        public static TotalPersonnel fromKorean(String korean) {
            return KoreanEnum.fromKorean(TotalPersonnel.class, korean);
        }
    }

    // 프로젝트 기간
    @AllArgsConstructor
    @Getter
    public enum ProjectPeriod implements KoreanEnum {
        ONE_MONTH("1개월"),
        THREE_MONTHS("3개월"),
        SIX_MONTHS("6개월"),
        ONE_YEAR_PLUS("1년 이상");

        @JsonValue
        private final String korean;

        @JsonCreator
        public static ProjectPeriod fromKorean(String korean) {
            return KoreanEnum.fromKorean(ProjectPeriod.class, korean);
        }
    }

    // 난이도
    @AllArgsConstructor
    @Getter
    public enum Difficulty implements KoreanEnum {
        BEGINNER("입문"),
        BASIC("초급"),
        MID("중급"),
        ADVANCED("고급");

        @JsonValue
        private final String korean;

        @JsonCreator
        public static Difficulty fromKorean(String korean) {
            return KoreanEnum.fromKorean(Difficulty.class, korean);
        }
    }

    // 대면/비대면
    @AllArgsConstructor
    @Getter
    public enum OnOff implements KoreanEnum {
        ON("대면"),
        OFF("비대면");

        @JsonValue
        private final String korean;

        @JsonCreator
        public static OnOff fromKorean(String korean) {
            return KoreanEnum.fromKorean(OnOff.class, korean);
        }
    }

    // 연령대
    @AllArgsConstructor
    @Getter
    public enum AgeGroup implements KoreanEnum {
        S10("10대"),
        S20A("20대"),
        S20B("30대"),
        S20C("40대 이상");

        @JsonValue
        private final String korean;

        @JsonCreator
        public static AgeGroup fromKorean(String korean) {
            return KoreanEnum.fromKorean(AgeGroup.class, korean);
        }
    }

    // ========= Method =========
    public void setStack(List<String> stack) {
        this.stack = String.join(",", stack);
    }

    public List<String> getStack() {
        return Arrays.asList(stack.split(","));
    }

    // 마감여부 확인(기한 지났으면 + 마감true이면)
    public boolean isClosed() {
        return this.recruitDueDate.isBefore(LocalDate.now()) || this.deadline;
    }

    public void approval(Apply apply) {
        if (!isClosed() && this.totalPersonnel.getValue() > this.currentPersonnel) {
            apply.approved();
            currentPersonnel++;
        }
    }

    public void reject(Apply apply) {
        if (!isClosed()) {
            apply.refused();
        }
    }

    // ========= 연관 관계 메서드 =========
    // 소프트스킬 추가, 삭제, 수정에 사용
    public void syncSoftSkillsWith(List<SoftSkill> newSkills) {
        List<SoftSkill> target = new ArrayList<>(newSkills);

        // 1) 기존에 연결됐으나 target에 없는 스킬 → 삭제
        Iterator<PostSoftSkill> iterator = postSoftSkills.iterator();
        while (iterator.hasNext()) {
            PostSoftSkill link = iterator.next();
            if (!target.contains(link.getSoftSkill())) {
                iterator.remove();
                link.getSoftSkill().getPostSoftSkills().remove(link);
                link.setPost(null);
                link.setSoftSkill(null);
            }
        }

        // 2) target에 있으나 아직 연결되지 않은 스킬 → 추가
        for (SoftSkill skill : target) {
            boolean exists = postSoftSkills.stream()
                    .anyMatch(link -> link.getSoftSkill().equals(skill));
            if (!exists) {
                PostSoftSkill link = new PostSoftSkill(this, skill);
                postSoftSkills.add(link);
                skill.getPostSoftSkills().add(link);
            }
        }
    }

    // 카테고리 추가, 삭제, 수정에 사용
    public void syncCategoriesWith(List<Category> newCategories) {
        List<Category> target = new ArrayList<>(newCategories);

        // 1) 삭제: 기존 연결됐으나 target에 없는 카테고리
        Iterator<PostCategory> it = postCategories.iterator();
        while (it.hasNext()) {
            PostCategory link = it.next();
            if (!target.contains(link.getCategory())) {
                it.remove();
                link.getCategory().getPostCategories().remove(link);
                link.setPost(null);
                link.setCategory(null);
            }
        }

        // 2) 추가: target에 있으나 아직 연결되지 않은 카테고리
        for (Category category : target) {
            boolean exists = postCategories.stream()
                    .anyMatch(link -> link.getCategory().equals(category));
            if (!exists) {
                PostCategory link = new PostCategory(this, category);
                postCategories.add(link);
                category.getPostCategories().add(link);
            }
        }
    }
}
