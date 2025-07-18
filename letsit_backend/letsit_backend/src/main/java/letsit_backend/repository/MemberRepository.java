package letsit_backend.repository;

import letsit_backend.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByUsername(String username);
    Optional<Member> findByUsername(String username);
}

