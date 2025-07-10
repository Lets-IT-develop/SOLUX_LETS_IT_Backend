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
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;


@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private Member member;

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

    @Column(nullable = false)
    private LocalDate projectStartDate;

    @Column(nullable = false)
    private LocalDate projectEndDate;

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
    @Builder.Default // null 대신 빈 리스트로 초기화
    private List<PostCategory> postCategories = new ArrayList<>();

    private int viewCount;

    private int scrapCount;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @Column(nullable = false)
    private Boolean deadline;


    @OneToMany(mappedBy = "post",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default // null 대신 빈 리스트로 초기화
    private List<PostSkillStack> postSkillStacks = new ArrayList<>();

    private String preference;

    @OneToMany(mappedBy = "post",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default // null 대신 빈 리스트로 초기화
    private List<PostSoftSkill> postSoftSkills = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgeGroup ageGroup;

    // 연령대 초반, 중반, 후반
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgeGroupDetail ageGroupDetail;

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
        S20("20대"),
        S30("30대"),
        S40("40대 이상");

        @JsonValue
        private final String korean;

        @JsonCreator
        public static AgeGroup fromKorean(String korean) {
            return KoreanEnum.fromKorean(AgeGroup.class, korean);
        }
    }

    // 연령대 상세 (초반, 중반, 후반)
    @AllArgsConstructor
    @Getter
    public enum AgeGroupDetail implements KoreanEnum {
        EARLY("초반"),
        MID("중반"),
        LATE("후반");

        @JsonValue
        private final String korean;

        @JsonCreator
        public static AgeGroupDetail fromKorean(String korean) {
            return KoreanEnum.fromKorean(AgeGroupDetail.class, korean);
        }
    }

    // ========= Method =========
    // 조회수 증가
    public void increaseViewCount() {
        this.viewCount += 1;
    }

    // 마감 처리
    public void setClosed() {
        this.deadline = true;
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

    // ========= 연관 관계 메서드 =========
    // 소프트스킬 추가, 삭제, 수정에 사용
    public void syncSoftSkillsWith(List<SoftSkill> newSkills) {
        syncLinks(
                postSoftSkills, // 현재 연결된 소프트스킬 리스트
                newSkills, // 새로 연결할 소프트스킬 리스트
                PostSoftSkill::getSoftSkill, // 기존 연결에서 SoftSkill 추출
                PostSoftSkill::new, // 새 PostSoftSkill 생성
                PostSoftSkill::unlink // 제거 시 역방향 정리s
        );
    }

    // 카테고리 추가, 삭제, 수정에 사용
    public void syncCategoriesWith(List<Category> newCategories) {
        syncLinks(
                postCategories, // 현재 연결된 카테고리 리스트
                newCategories, // 새로 연결할 카테고리 리스트
                PostCategory::getCategory, // 기존 연결에서 Category 추출
                PostCategory::new, // 새 PostCategory 생성
                PostCategory::unlink // 제거 시 역방향 정리
        );
    }

    // 기술스택 추가, 삭제, 수정에 사용
    public void syncSkillStacksWith(List<SkillStack> newStacks) {
        syncLinks(
                postSkillStacks,           // 현재 연결 리스트
                newStacks,                 // 새로 연결할 대상 리스트
                PostSkillStack::getSkillStack,  // 기존 연결에서 SkillStack 추출
                PostSkillStack::new,            // 새 PostSkillStack 생성
                PostSkillStack::unlink // 제거 시 역방향 정리s
        );
    }

    // 연관 관계 동기화 메서드 공통 로직 분리

    /**
     * 기존 연관관계 리스트(existingLinks)를 새로운 리스트(newItems)로 동기화한다.
     * 삭제 대상은 지우고, 추가 대상은 새로 연결 객체를 만들어 추가한다.
     *
     * @param existingLinks 현재 Post와 연결된 중간 엔티티 리스트 (ex: postSkillStacks)
     * @param newItems      새롭게 설정할 대상 리스트 (ex: List<SkillStack>)
     * @param getItem       중간 엔티티에서 실제 참조 대상(T)을 추출하는 getter (ex: PostSkillStack::getSkillStack)
     * @param createLink    Post와 대상 T로부터 중간 엔티티를 생성하는 함수 (ex: new PostSkillStack(this, stack))
     * @param removeLink    연결 해제 시 후처리 로직 (양방향 관계 끊기, null 처리 등)
     */
    private <T, L> void syncLinks(
            List<L> existingLinks,
            List<T> newItems,
            Function<L, T> getItem,
            BiFunction<Post, T, L> createLink,
            Consumer<L> removeLink
    ) {
        // 1. 삭제 단계: 기존 연결 중 newItems에 포함되지 않은 항목 제거
        Iterator<L> iterator = existingLinks.iterator();
        while (iterator.hasNext()) {
            L link = iterator.next();
            if (!newItems.contains(getItem.apply(link))) {
                iterator.remove();     // 현재 Post → 해당 연결 제거
                removeLink.accept(link); // 역방향(SoftSkill, Category 등)에서도 제거 처리
            }
        }

        // 2. 추가 단계: newItems 중 아직 연결되지 않은 항목을 새로 추가
        for (T item : newItems) {
            boolean exists = existingLinks.stream()
                    .anyMatch(link -> getItem.apply(link).equals(item));
            if (!exists) {
                L newLink = createLink.apply(this, item); // 새 연결 객체 생성
                existingLinks.add(newLink);               // 현재 Post에 추가
            }
        }
    }

    // 게시글 수정
    public void updatePost(String title,
                           String content,
                           TotalPersonnel totalPersonnel,
                           LocalDate recruitDueDate,
                           LocalDate projectStartDate,
                           LocalDate projectEndDate,
                           Difficulty difficulty,
                           OnOff onOff,
                           Area region,
                           Area subRegion,
                           String preference,
                           AgeGroup ageGroup,
                           AgeGroupDetail ageGroupDetail) {
        this.title = title;
        this.content = content;
        this.totalPersonnel = totalPersonnel;
        this.recruitDueDate = recruitDueDate;
        this.projectStartDate = projectStartDate;
        this.projectEndDate = projectEndDate;
        this.difficulty = difficulty;
        this.onOff = onOff;
        this.region = region;
        this.subRegion = subRegion;
        this.preference = preference;
        this.ageGroup = ageGroup;
        this.ageGroupDetail = ageGroupDetail;
    }

}
