package letsit_backend.init;

import jakarta.annotation.PostConstruct;
import letsit_backend.model.Category;
import letsit_backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InitCategoryData {
    private final CategoryRepository categoryRepository;

    @PostConstruct
    public void init() {
        // 카테고리 데이터를 초기화하는 메서드를 호출
        initCategories();
    }

    private void initCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> categories = List.of(
                    new Category("프론트엔드 개발"),
                    new Category("백엔드 개발"),
                    new Category("풀스택 개발"),
                    new Category("모바일 개발"),
                    new Category("데브옵스"),
                    new Category("데이터 엔지니어링"),
                    new Category("인공지능/머신러닝"),
                    new Category("게임 개발"),
                    new Category("웹 디자인"),
                    new Category("UI/UX 디자인"),
                    new Category("QA/테스트"),
                    new Category("프로젝트 관리"),
                    new Category("기타")
            );


            // 카테고리 데이터를 데이터베이스에 저장
            categoryRepository.saveAll(categories);
        }
    }
}
