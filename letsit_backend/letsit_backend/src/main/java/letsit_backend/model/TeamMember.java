package letsit_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TeamMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teamMemberId;

    @ManyToOne
    @JoinColumn(name = "TEAM_ID")
    private TeamPost teamId;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private Member userId;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    private Role teamMemberRole;
    public enum Role {
        Team_Leader,
        Team_Member
    }

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
    }

    public void setTeamMemberRole(Role teamMemberRole) {
        this.teamMemberRole = teamMemberRole;
    }
}
