package letsit_backend.repository;

import letsit_backend.model.SkillStack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SkillStackRepository extends JpaRepository<SkillStack, Long> {
    List<SkillStack> findAllByStackNameIn(Collection<String> names);
}
