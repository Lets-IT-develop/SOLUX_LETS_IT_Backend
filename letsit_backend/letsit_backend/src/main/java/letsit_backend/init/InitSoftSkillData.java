package letsit_backend.init;

import jakarta.annotation.PostConstruct;
import letsit_backend.model.SoftSkill;
import letsit_backend.repository.SoftSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InitSoftSkillData {
    private final SoftSkillRepository softSkillRepository;

    @PostConstruct
    public void init() {
        // SoftSkill 데이터를 초기화하는 메서드를 호출
        initSoftSkills();
    }

    /**
     * 소프트스킬 데이터를 초기화하는 메서드
     * 이 메서드는 애플리케이션 시작 시 한 번만 실행되어 지역 데이터를 데이터베이스에 저장
     */
    public void initSoftSkills() {
        if (softSkillRepository.count() == 0) {
            List<SoftSkill> softSkills = List.of(
                    new SoftSkill("회의를 효율적으로 진행해요", SoftSkill.SoftSkillSection.LEADERSHIP),
                    new SoftSkill("통솔력이 있어요", SoftSkill.SoftSkillSection.LEADERSHIP),
                    new SoftSkill("외부 PR을 잘해요", SoftSkill.SoftSkillSection.LEADERSHIP),
                    new SoftSkill("공동 목표를 잘 유지할 수 있게 리마인드해요", SoftSkill.SoftSkillSection.LEADERSHIP),
                    new SoftSkill("문제 해결을 잘 해요", SoftSkill.SoftSkillSection.CRITICAL_THINKING),
                    new SoftSkill("변화에 빠르게 적응해요.", SoftSkill.SoftSkillSection.CRITICAL_THINKING),
                    new SoftSkill("습득력이 빨라요", SoftSkill.SoftSkillSection.CRITICAL_THINKING),
                    new SoftSkill("결정을 잘해요", SoftSkill.SoftSkillSection.CRITICAL_THINKING),
                    new SoftSkill("복잡한 문제를 단순하게 정리해요", SoftSkill.SoftSkillSection.CRITICAL_THINKING),
                    new SoftSkill("위기대처능력이 좋아요", SoftSkill.SoftSkillSection.CRITICAL_THINKING),
                    new SoftSkill("피드백에 열려있어요", SoftSkill.SoftSkillSection.CRITICAL_THINKING),
                    new SoftSkill("학습에 적극적이에요", SoftSkill.SoftSkillSection.WORK_ATTITUDE),
                    new SoftSkill("문서화에 일가견이 있어요", SoftSkill.SoftSkillSection.WORK_ATTITUDE),
                    new SoftSkill("계획적으로 일해요", SoftSkill.SoftSkillSection.WORK_ATTITUDE),
                    new SoftSkill("열정이 넘쳐요", SoftSkill.SoftSkillSection.WORK_ATTITUDE),
                    new SoftSkill("일정을 철저히 지켜요", SoftSkill.SoftSkillSection.WORK_ATTITUDE),
                    new SoftSkill("끊임없이 성장해요", SoftSkill.SoftSkillSection.WORK_ATTITUDE),
                    new SoftSkill("책임감이 강해요", SoftSkill.SoftSkillSection.WORK_ATTITUDE),
                    new SoftSkill("세부 사항을 꼼꼼하게 확인해요", SoftSkill.SoftSkillSection.WORK_ATTITUDE),
                    new SoftSkill("체계적으로 업무를 계획하고 실행해요", SoftSkill.SoftSkillSection.WORK_ATTITUDE),
                    new SoftSkill("시간을 효율적으로 관리해요", SoftSkill.SoftSkillSection.WORK_ATTITUDE),
                    new SoftSkill("마감기한을 잘 지켜요", SoftSkill.SoftSkillSection.WORK_ATTITUDE),
                    new SoftSkill("끈기있게 임해요", SoftSkill.SoftSkillSection.WORK_ATTITUDE),
                    new SoftSkill("지시 없이도 자기주도적으로 일할 수 있어요", SoftSkill.SoftSkillSection.WORK_ATTITUDE),
                    new SoftSkill("책임감이 있어요", SoftSkill.SoftSkillSection.WORK_ATTITUDE),
                    new SoftSkill("끈기있어요", SoftSkill.SoftSkillSection.WORK_ATTITUDE),
                    new SoftSkill("의견 조율에 능해요", SoftSkill.SoftSkillSection.TEAMWORK),
                    new SoftSkill("협업에 능해요", SoftSkill.SoftSkillSection.TEAMWORK),
                    new SoftSkill("동료 의견을 존중해요", SoftSkill.SoftSkillSection.TEAMWORK),
                    new SoftSkill("분위기를 편하게 만들 줄 알아요", SoftSkill.SoftSkillSection.TEAMWORK),
                    new SoftSkill("커뮤니케이션에 능해요", SoftSkill.SoftSkillSection.TEAMWORK),
                    new SoftSkill("아이스브레이킹에 자신 있어요", SoftSkill.SoftSkillSection.TEAMWORK),
                    new SoftSkill("설명을 잘해요", SoftSkill.SoftSkillSection.COLLABORATION_STYLE),
                    new SoftSkill("소통이 빨라요", SoftSkill.SoftSkillSection.COLLABORATION_STYLE),
                    new SoftSkill("창의적인 아이디어가 많아요", SoftSkill.SoftSkillSection.COLLABORATION_STYLE),
                    new SoftSkill("잘 경청하고 역지사지해요", SoftSkill.SoftSkillSection.COLLABORATION_STYLE),
                    new SoftSkill("연락을 잘봐요", SoftSkill.SoftSkillSection.COLLABORATION_STYLE),
                    new SoftSkill("시간을 잘 지켜요", SoftSkill.SoftSkillSection.COLLABORATION_STYLE),
                    new SoftSkill("다른 파트와의 소통에 능해요", SoftSkill.SoftSkillSection.COLLABORATION_STYLE)
            );

            // 소프트스킬 데이터를 데이터베이스에 저장
            softSkillRepository.saveAll(softSkills);
        }
    }
}
