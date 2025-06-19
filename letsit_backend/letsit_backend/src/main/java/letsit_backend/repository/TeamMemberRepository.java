package letsit_backend.repository;

import jakarta.persistence.LockModeType;
import letsit_backend.model.Member;
import letsit_backend.model.TeamMember;
import letsit_backend.model.TeamPost;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findAllByTeamId(TeamPost teamId);
    // @EntityGraph(attributePaths = {"member", "member.profile"})
    List<TeamMember> findAllByTeamPost(TeamPost teamPost);
    List<TeamMember> findByTeamId_TeamId(Long teamId);
    List<TeamMember> findAllByUserId(Member member);
    Optional<TeamMember> findByMemberAndTeamPost(Member member, TeamPost teamPost);
    boolean existsByMemberAndTeamPost(Member member, TeamPost teamPost);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT tm FROM TeamMember tm WHERE tm.teamId = :teamId")
    List<TeamMember> findAllByTeamPostWithLock(@Param("teamId") TeamPost teamPost);


}
