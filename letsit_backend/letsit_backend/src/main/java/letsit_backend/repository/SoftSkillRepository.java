package letsit_backend.repository;

import letsit_backend.model.SoftSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SoftSkillRepository extends JpaRepository<SoftSkill, Long> {
    List<SoftSkill> findAllBySkillNameIn(Collection<String> names);
}
