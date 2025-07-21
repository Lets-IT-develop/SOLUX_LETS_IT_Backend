package letsit_backend.init;

import jakarta.annotation.PostConstruct;
import letsit_backend.model.Interest;
import letsit_backend.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InitInterestData {
    private final InterestRepository interestRepository;

    @PostConstruct
    public void init() {
        initInterest();
    }

    private void initInterest() {
        if (interestRepository.count() == 0) {
            List<Interest> interests = List.of(
                    new Interest("기획자"),
                    new Interest("개발자"),
                    new Interest("디자이너"),
                    new Interest("데이터 엔지니어"),
                    new Interest("AI 엔지니어"),
                    new Interest("ML 엔지니어")
            );
            interestRepository.saveAll(interests);
        }
    }
}
