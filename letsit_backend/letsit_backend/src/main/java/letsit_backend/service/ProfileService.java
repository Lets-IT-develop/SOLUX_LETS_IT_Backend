package letsit_backend.service;

import letsit_backend.dto.profile.ProfileRequestDto;
import letsit_backend.dto.profile.ProfileResponseDto;
import letsit_backend.dto.profile.ProfileUpdateRequestDto;
import letsit_backend.model.Member;
import letsit_backend.model.Profile;
import letsit_backend.repository.MemberRepository;
import letsit_backend.repository.ProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;

    public ProfileService(MemberRepository memberRepository, ProfileRepository profileRepository) {
        this.memberRepository = memberRepository;
        this.profileRepository = profileRepository;
    }

    public ProfileResponseDto getProfileInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        Profile profile = profileRepository.findByMember(member)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 프로필이 존재하지 않습니다."));

        return new ProfileResponseDto(
                profile.getNickname(),
                profile.getProfileImageUrl(),
                profile.getInterests(),
                profile.getSkills(),
                profile.getBio(),
                profile.getSns()
        );
    }

    public void createProfile(Long userId, ProfileRequestDto profileRequestDto) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾지 못했습니다"));

        Profile profile = new Profile(
                member,
                profileRequestDto.getProfileImageUrl(), // 프로필 이미지 어떤 식으로
                profileRequestDto.getNickname(),
                profileRequestDto.getAgeGroup(),
                profileRequestDto.getAgeDetail(),
                profileRequestDto.getInterests()
        );

        profileRepository.save(profile);
    }

    public void updateProfile(Long userId, ProfileUpdateRequestDto profileUpdateRequestDto) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾지 못했습니다."));
        Profile profile = profileRepository.findByMember(member)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾지 못했습니다."));

        Profile updatedProfile = Profile.builder()
                .profileId(profile.getProfileId())
                .userId(member)
                .nickname(profileUpdateRequestDto.getNickname())
                .profileImageUrl(profileUpdateRequestDto.getProfileImageUrl())
                .interests(profileUpdateRequestDto.getInterests())
                .bio(profileUpdateRequestDto.getBio())
                .build();

        profileRepository.save(updatedProfile);
    }
}
