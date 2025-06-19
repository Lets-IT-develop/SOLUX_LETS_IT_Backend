package letsit_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class TeamPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teamId;

    @OneToOne
    @JoinColumn(name = "POST_ID")
    private Post post;

    @Column(nullable = false)
    private String prjTitle;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isComplete = false;


    public void updateComplete() {
        if (!this.isComplete) {
            this.isComplete = true;
        }
    }

}
