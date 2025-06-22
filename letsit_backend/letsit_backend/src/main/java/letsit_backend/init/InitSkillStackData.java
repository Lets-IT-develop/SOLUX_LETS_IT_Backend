package letsit_backend.init;

import jakarta.annotation.PostConstruct;
import letsit_backend.model.SkillStack;
import letsit_backend.repository.SkillStackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InitSkillStackData {
    private final SkillStackRepository skillStackRepository;

    @PostConstruct
    public void init() {
        // SkillStack 데이터를 초기화하는 메서드를 호출
        initSkillStacks();
    }

    /**
     * 기술 스택 데이터를 초기화하는 메서드
     * 이 메서드는 애플리케이션 시작 시 한 번만 실행되어 지역 데이터를 데이터베이스에 저장
     */
    private void initSkillStacks() {
        if (skillStackRepository.count() == 0) {
            List<SkillStack> skillStacks = List.of(
                    new SkillStack("pyhton"),
                    new SkillStack("java"),
                    new SkillStack("javascript"),
                    new SkillStack("c++"),
                    new SkillStack("c#"),
                    new SkillStack("swift"),
                    new SkillStack("kotlin"),
                    new SkillStack("typescript"),
                    new SkillStack("php"),
                    new SkillStack("html"),
                    new SkillStack("css"),
                    new SkillStack("sql"),
                    new SkillStack("react"),
                    new SkillStack("vue"),
                    new SkillStack("node.js"),
                    new SkillStack("django"),
                    new SkillStack("flask"),
                    new SkillStack("spring"),
                    new SkillStack("express"),
                    new SkillStack("next.js"),
                    new SkillStack("flutter"),
                    new SkillStack("docker"),
                    new SkillStack("kubernetes"),
                    new SkillStack("aws"),
                    new SkillStack("machine learning"),
                    new SkillStack("deep learning"),
                    new SkillStack("data science"),
                    new SkillStack("ai")
            );

            // skillStacks를 데이터베이스에 저장
            skillStackRepository.saveAll(skillStacks);
        }
    }
}
