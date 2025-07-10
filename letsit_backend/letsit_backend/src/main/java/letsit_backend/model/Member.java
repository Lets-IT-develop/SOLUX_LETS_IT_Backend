package letsit_backend.model;

import jakarta.persistence.*;
import lombok.*;


@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column
    private String username;

    @Column
    private String email;

    @Column
    private String name;

    @Column
    private String ageRange;

    @Column
    private String gender;

    @Column
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    private Role role;

    public Member updateInfo(String email, String name) {
        this.email = email;
        this.name = name;
        return this;
    }
}
