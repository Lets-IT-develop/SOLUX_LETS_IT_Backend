package letsit_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SkillStack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long skillStackId;

    @Column(nullable = false)
    private String stackName;

    @OneToMany(mappedBy = "skillStack",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PostSkillStack> postStacks = new ArrayList<>();

    // init용 Constructor
    public SkillStack(String stackName) {
        this.stackName = stackName;
    }
}
