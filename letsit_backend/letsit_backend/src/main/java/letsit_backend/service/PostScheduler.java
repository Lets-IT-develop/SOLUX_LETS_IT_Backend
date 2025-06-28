package letsit_backend.service;

import letsit_backend.model.Post;
import letsit_backend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PostScheduler {
    private final PostRepository postRepository;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    @Transactional
    public void closeExpiredPosts() {
        LocalDate today = LocalDate.now();
        List<Post> openPosts = postRepository.findAllByDeadlineFalse();
        // 만료된 게시글을 찾아서 상태를 업데이트
        for (Post post : openPosts) {
            if (post.getRecruitDueDate().isBefore(today)) {
                post.setDeadline(true);
            }
        }
    }
}
