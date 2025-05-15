package letsit_backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Arrays;
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

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private Member userId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    private TotalPersonnel totalPersonnel;

    public enum TotalPersonnel {
        TWO("2명", 2),
        THREE("3명", 3),
        FOUR("4명", 4),
        FIVE("5명", 5),
        SIX("6명", 6),
        SEVEN("7명", 7),
        EIGHT("8명", 8);

        private final String korean;
        private final int value;

        TotalPersonnel(String korean, int value) {
            this.korean = korean;
            this.value = value;
        }

        @JsonValue
        public String getKorean() {
            return korean;
        }

        public int getValue() {
            return value;
        }

        @JsonCreator
        public static TotalPersonnel fromKorean(String korean) {
            for (TotalPersonnel num : TotalPersonnel.values()) {
                if (num.korean.equals(korean)) {
                    return num;
                }
            }
            throw new IllegalArgumentException("Unknown enum value: " + korean);
        }
    }


    @ColumnDefault("0")
    private int currentPersonnel;

    // TODO 기간어떻게받을지...
    @Column(nullable = false)
    private LocalDate recruitDueDate;

    @Enumerated(EnumType.STRING)
    private ProjectPeriod projectPeriod;

    public enum ProjectPeriod {
        oneMonth("1개월"),
        twoMonths("3개월"),
        threeMonths("6개월"),
        fourMonths("1년 이상");

        private final String korean;

        ProjectPeriod(String korean) {
            this.korean = korean;
        }

        @JsonValue
        public String getKorean() {
            return korean;
        }

        @JsonCreator
        public static ProjectPeriod fromKorean(String korean) {
            for (ProjectPeriod period : ProjectPeriod.values()) {
                if (period.korean.equals(korean)) {
                    return period;
                }
            }
            throw new IllegalArgumentException("Unknown enum value: " + korean);
        }
    }

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    public enum Difficulty {
        BEGINNER("입문"),
        BASIC("초급"),
        MID("중급"),
        ADVANCED("고급");

        private final String korean;

        Difficulty(String korean) {
            this.korean = korean;
        }

        @JsonValue
        public String getKorean() {
            return korean;
        }

        @JsonCreator
        public static Difficulty fromKorean(String korean) {
            if (korean == null || korean.isEmpty()) {
                throw new IllegalArgumentException("Empty string is not a valid value for Difficulty");
            }
            for (Difficulty difficulty : Difficulty.values()) {
                if (difficulty.korean.equals(korean)) {
                    return difficulty;
                }
            }
            throw new IllegalArgumentException("Unknown enum value: " + korean);
        }
    }


    @Enumerated(EnumType.STRING)
    private OnOff onOff;

    public enum OnOff {
        ON("대면"),
        OFF("비대면");

        private final String korean;

        OnOff(String korean) {
            this.korean = korean;
        }

        @JsonValue
        public String getKorean() {
            return korean;
        }

        @JsonCreator
        public static OnOff fromKorean(String korean) {
            if (korean == null || korean.isEmpty()) {
                throw new IllegalArgumentException("Empty string is not a valid value for OnOff");
            }
            for (OnOff onOff : OnOff.values()) {
                if (onOff.korean.equals(korean)) {
                    return onOff;
                }
            }
            throw new IllegalArgumentException("Unknown enum value: " + korean);
        }
    }

    @ManyToOne
    @JoinColumn(name = "region_id")
    private Area region;

    @ManyToOne
    @JoinColumn(name = "sub_region_id")
    private Area subRegion;


    @Column(name = "category_id")
    private String categoryId;

    public void setCategoryId(List<String> categoryId) {
        this.categoryId = String.join(",", categoryId);
    }

    public List<String> getCategoryId() {
        return Arrays.asList(categoryId.split(","));
    }

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

    public void setStack(List<String> stack) {
        this.stack = String.join(",", stack);
    }

    public List<String> getStack() {
        return Arrays.asList(stack.split(","));
    }


    private String preference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgeGroup ageGroup;

    public enum AgeGroup {
        S10("10대"),
        S20A("20대"),
        S20B("30대"),
        S20C("40대 이상");

        private final String korean;

        AgeGroup(String korean) {
            this.korean = korean;
        }

        @JsonValue
        public String getKorean() {
            return korean;
        }

        @JsonCreator
        public static AgeGroup fromKorean(String korean) {
            for (AgeGroup ageGroup : AgeGroup.values()) {
                if (ageGroup.korean.equals(korean)) {
                    return ageGroup;
                }
            }
            throw new IllegalArgumentException("Unknown enum value: " + korean);
        }
    }

    public void setDeadline(Boolean deadline) {
        this.deadline = deadline;
    }

    // 마감여부 확인(기한 지났으면 + 마감true이면)
    public boolean isClosed() {
        return this.recruitDueDate.isBefore(LocalDate.now()) || this.deadline;
    }

    public void approval(Apply apply) {
        if (!isClosed() && this.totalPersonnel.getValue() > this.currentPersonnel) {
            apply.approve();
            currentPersonnel++;
        }
    }

    public void reject(Apply apply) {
        if (!isClosed()) {
            apply.refuse();
        }
    }
}
