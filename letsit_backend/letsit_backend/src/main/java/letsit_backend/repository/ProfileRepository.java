package letsit_backend.repository;

import letsit_backend.model.Member;
import letsit_backend.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Profile findByMember(Member member);

    List<Profile> findByMemberIn(List<Member> members);
}
