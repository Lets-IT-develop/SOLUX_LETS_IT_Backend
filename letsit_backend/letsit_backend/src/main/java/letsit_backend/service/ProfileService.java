package letsit_backend.service;

import letsit_backend.dto.profile.*;
import letsit_backend.exception.CommonErrorCode;
import letsit_backend.exception.CustomException;
import letsit_backend.exception.ProfileErrorCode;
import letsit_backend.model.Member;
import letsit_backend.model.Profile;
import letsit_backend.repository.MemberRepository;
import letsit_backend.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProfileService {

    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;

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
                .skills(profile.getSkills())
                .bio(profile.getBio())
                .sns(profile.getSns())
                .build();
    }

    // 프로필 생성
    public void createProfile(Long userId, ProfileRequestDto profileRequestDto) {
        Member member = findMemberById(userId);

        Profile profile = new Profile(
                member,
                profileRequestDto.getProfileImageUrl(), // 프로필 이미지 어떤 식으로
                profileRequestDto.getNickname(),
                profileRequestDto.getAgeGroup(),
                profileRequestDto.getAgeDetail(),
                profileRequestDto.getInterests()
                // TODO 개인 소개
        );

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

    // TODO 머지 후 스킬 관련 로직 작성

    // sns 변수 타입 고민
    public void createSNS(Long userId, SNSRequestDto snsRequestDto) {
        Member member = findMemberById(userId);
        Profile profile = findProfileByMember(member);

        profile.createSNS(snsRequestDto.getSns());
    }
}
