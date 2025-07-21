package letsit_backend.service;

import letsit_backend.dto.profile.*;
import letsit_backend.exception.CommonErrorCode;
import letsit_backend.exception.CustomException;
import letsit_backend.exception.ProfileErrorCode;
import letsit_backend.model.Member;
import letsit_backend.model.Profile;
import letsit_backend.model.SkillStack;
import letsit_backend.repository.MemberRepository;
import letsit_backend.repository.ProfileRepository;
import letsit_backend.repository.SkillStackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProfileService {

    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;
    private final SkillStackRepository skillStackRepository;

    private Member findMemberById(Long userId) {
        if (userId == null) {
            throw new CustomException(CommonErrorCode.MISSING_PARAMETER);
        }

        return memberRepository.findById(userId)
                .orElseThrow(ProfileErrorCode.USER_NOT_FOUND::getDefaultException);
    }

    private Profile findProfileByMember(Member member) {
        Profile profile = profileRepository.findByMember(member);
        if (profile == null) {
            throw new CustomException(ProfileErrorCode.PROFILE_NOT_FOUND);
        }
        return profile;
    }

    public ProfileResponseDto getProfileInfo(Long userId) {
        Member member = findMemberById(userId);

        Profile profile = findProfileByMember(member);

        return ProfileResponseDto.builder()
                .nickname(profile.getNickname())
                .profileImageUrl(profile.getProfileImageUrl())
                .interests(profile.getInterests())
                .skillStacks(profile.getSkillStacks())
                .bio(profile.getBio())
                .sns(profile.getSns())
                .build();
    }

    // 프로필 생성
    // TODO 초기에 안 받는 값들 초기화시켜놓고 이후에 수정하는 식으로
    public void createProfile(Long userId, ProfileRequestDto profileRequestDto) {
        Member member = findMemberById(userId);

        Profile profile = Profile.builder()
                .member(member)
                .profileImageUrl(profileRequestDto.getProfileImageUrl())
                .nickname(profileRequestDto.getNickname())
                .ageGroup(profileRequestDto.getAgeGroup())
                .ageGroupDetail(profileRequestDto.getAgeGroupDetail())
                .interests(profileRequestDto.getInterests())
                .softSkills(profileRequestDto.getSoftSkills())
                .build();

        profileRepository.save(profile);
    }

    // 프로필 수정
    public void updateProfile(Long userId, ProfileUpdateRequestDto profileUpdateRequestDto) {
        Member member = findMemberById(userId);
        Profile profile = findProfileByMember(member);

        Profile updatedProfile = Profile.builder()
                .profileId(profile.getProfileId())
                .member(member)
                .nickname(profileUpdateRequestDto.getNickname())
                .profileImageUrl(profileUpdateRequestDto.getProfileImageUrl())
                .interests(profileUpdateRequestDto.getInterests())
                .bio(profileUpdateRequestDto.getBio())
                .build();

        profileRepository.save(updatedProfile);
    }

    public void createSkillStack(Long userId, SkillStackRequestDto skillStackRequestDto) {
        Member member = findMemberById(userId);
        Profile profile = findProfileByMember(member);

        List<SkillStack> skillStacks = skillStackRequestDto.getSkillStackIds().stream()
                .map(id -> skillStackRepository.findById(id)
                        .orElseThrow(() -> new CustomException(ProfileErrorCode.SKILLSTACK_NOT_FOUND)))
                .toList();

        profile.getSkillStacks().clear();
        profile.getSkillStacks().addAll(skillStacks);

        profileRepository.save(profile);
    }

    public void createSNS(Long userId, SNSRequestDto snsRequestDto) {
        Member member = findMemberById(userId);
        Profile profile = findProfileByMember(member);

        profile.createSNS(snsRequestDto.getSns());
    }
}
