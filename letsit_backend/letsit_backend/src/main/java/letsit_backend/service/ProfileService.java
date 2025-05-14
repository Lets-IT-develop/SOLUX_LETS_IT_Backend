package letsit_backend.service;

import jakarta.transaction.Transactional;
import letsit_backend.dto.profile.ProfileDto;
import letsit_backend.dto.profile.ProfileRequestDto;
import letsit_backend.model.Member;
import letsit_backend.model.Profile;
import letsit_backend.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@Service
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Transactional
    public List<Profile> getAllProfiles() {
        return profileRepository.findAll();
    }

    @Transactional
    public Profile getProfileById(long id) {
        return profileRepository.findById(id).orElse(null);
    }

    @Transactional
    public Profile saveProfile(Profile profile) {
        return profileRepository.save(profile);
    }

    @Transactional
    public void deleteProfileById(long id) {
        profileRepository.deleteById(id);
    }

    @Transactional
    public Profile createOrUpdateProfile(ProfileRequestDto profileDto, Member member) {
        //member.setUserId(profileDto.getUserId());
        if (!member.getUserId().equals(profileDto.getUserId())) {
            throw new AccessDeniedException("You do not have permission to modify this profile");
        }
        /*
        if (member == null) {
            throw new IllegalArgumentException("유효하지 않은 USER ID " + profileDto.getUserId());
        }

         */
        Profile profile = profileRepository.findByMember(member);

        if (profile == null) {
            profile = new Profile();
            profile.setMember(member);
            profile.setMannerScore(75.0); // 기본값 설정
            profile.setMannerTier(Profile.Manner_tier.B); // 기본값 설정
            //profile.setNickname(member.getName());
        }
        profile.setName(member.getName());
        profile.setAge(profileDto.getAge());
        profile.setProfileImageUrl(profileDto.getProfileImageUrl());
        return profileRepository.save(profile);
    }

    @Transactional
    public Profile updateProfile(ProfileDto profileDto, Member member) {
        //member.setUserId(profileDto.getUserId());
        if (!member.getUserId().equals(profileDto.getUserId())) {
            throw new AccessDeniedException("You do not have permission to modify this profile");
        }

        Profile profile = profileRepository.findByMember(member);
        /*
        if (member == null) {
            throw new IllegalArgumentException("유효하지 않은 userId " + profileDto.getUserId());
        }

         */

        if (profile == null) {
            throw new IllegalArgumentException("프로필이 존재하지 않습니다.");
        }
        if (profileDto.getNickname() != null) {
            profile.setNickname(profileDto.getNickname());
        }
        if (profileDto.getMannerTier() != null) {
            profile.setMannerTier(profileDto.getMannerTier());
        } else {
            profile.setMannerTier(Profile.Manner_tier.B); // 기본값 설정
        }
        if (profileDto.getMannerScore() != 0) {
            profile.setMannerScore(profileDto.getMannerScore());
        } else {
            profile.setMannerScore(75.0); // 기본값 설정
        }
        if (profileDto.getSns() != null) {
            profile.setSns(profileDto.getSns());
        }
        if (profileDto.getProfileImageUrl() != null) {
            profile.setProfileImageUrl(profileDto.getProfileImageUrl());
        }
        if (profileDto.getBio() != null) {
            profile.setBio(profileDto.getBio());
        }
        if (profileDto.getSelfIntro() != null) {
            profile.setSelfIntro(profileDto.getSelfIntro());
        }
        if (profileDto.getSkills() != null) {
            profile.setSkills(profileDto.getSkills());
        }
        return profileRepository.save(profile);
    }

    @Transactional
    public void updateMannerTier(Member userId) {
        Profile profile = profileRepository.findByMember(userId);
        profile.updateMannerTier();
        profileRepository.save(profile);
    }
}
