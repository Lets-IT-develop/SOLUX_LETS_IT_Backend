package letsit_backend.repository;

import letsit_backend.model.Member;
import letsit_backend.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Profile findByMember(Member member);

    // Apply에서 사용; SELECT * FROM Profiles WHERE user_id IN (리스트에 있는 멤버 객체들 열거)
    List<Profile> findByMemberIn(List<Member> members);
}
