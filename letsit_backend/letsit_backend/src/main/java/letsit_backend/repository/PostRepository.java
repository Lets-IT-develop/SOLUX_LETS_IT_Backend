package letsit_backend.repository;

import letsit_backend.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import letsit_backend.model.Member;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.Query;


public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    //한개의 키워드
    List<Post> findByTitleContainingOrContentContaining(String title, String content, Sort sort);

    // 페이징
    Page<Post> findByTitleContaining(String title, Pageable pageable);

    List<Post> findByMemberAndDeadlineFalse(Member userId);

    List<Post> findAllByDeadlineFalseOrderByCreatedAtDesc();

    List<Post> findAllByDeadlineFalse();
}
