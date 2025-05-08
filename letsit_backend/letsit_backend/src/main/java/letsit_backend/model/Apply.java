package letsit_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Timestamp;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor // TODO 해당문법의 쓰임확인하기
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Apply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applyId;

    @ManyToOne // 다대일
    @JoinColumn(name = "POST_ID")
    private Post postId;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private Member member;

    @NotEmpty
    private String preferStack;

    @NotEmpty
    private String desiredField;

    @Column(nullable = false)
    private String applyContent;

    @NotEmpty
    private String contact;

    @Column(nullable = false)
    @CreatedDate
    private Timestamp applyCreateDate;

    private Boolean confirm;


    public void approved() {
        this.confirm = true;
    }

    public boolean isApproved() {
        return Boolean.TRUE.equals(this.confirm);
    }

    public void refused() {
        this.confirm = false;
    }

    public boolean isNullYet() {
        return this.confirm == null;
    }
}
