package letsit_backend.service;

import letsit_backend.dto.profile.*;
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

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
    }

    private Profile findProfileByMember(Member member) {
        return profileRepository.findByMember(member)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 프로필이 존재하지 않습니다."));
    }

    // 프로필 정보 조회
    public ProfileResponseDto getProfileInfo(Long memberId) {
        Member member = findMemberById(memberId);

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
    public void createProfile(Long memberId, ProfileRequestDto profileRequestDto) {
        Member member = findMemberById(memberId);

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
    public void updateProfile(Long memberId, ProfileUpdateRequestDto profileUpdateRequestDto) {
        Member member = findMemberById(memberId);
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
    public void createSNS(Long memberId, SNSRequestDto snsRequestDto) {
        Member member = findMemberById(memberId);
        Profile profile = findProfileByMember(member);

        profile.createSNS(snsRequestDto.getSns());
    }
}
