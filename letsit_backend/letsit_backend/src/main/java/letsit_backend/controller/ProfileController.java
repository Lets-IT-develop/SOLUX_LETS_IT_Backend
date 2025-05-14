package letsit_backend.controller;

import letsit_backend.CurrentUser;
import letsit_backend.dto.Response;
import letsit_backend.dto.profile.ProfileDto;
import letsit_backend.dto.profile.ProfileResponseDto;
import letsit_backend.model.Member;
import letsit_backend.model.Profile;
import letsit_backend.service.MemberService;
import letsit_backend.service.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @Autowired
    private ProfileService profileService;

    @Autowired
    private MemberService memberService;


    @GetMapping("/all")
    public List<ProfileResponseDto> getAllProfiles() {
        logger.debug("모든 프로필 조회 요청");
        List<Profile> profiles = profileService.getAllProfiles();
        return profiles.stream().map(this::convertToResponseDto).collect(Collectors.toList());
    }

    //자기 자신 프로필 조회
    @GetMapping("/my")
    public Response<ProfileResponseDto> getMyProfile(@CurrentUser Member member) {
        if (member == null) {
            return Response.fail("미인증 회원");
        }
        logger.debug("현재 사용자 프로필 조회 요청: {}", member.getUserId());
        Profile profile = profileService.getProfileById(member.getUserId());
        if (profile == null) {
            return Response.fail("프로필을 찾을 수 없습니다.");
        }
        logger.debug("조회된 프로필: {}", profile);
        ProfileResponseDto profileResponseDto = convertToResponseDto(profile);
        return Response.success("나의 프로필 조회 성공", profileResponseDto);
    }

    //다른 사용자 프로필 조회
    @GetMapping("/{userId}")
    public Response<ProfileResponseDto> getProfileById(@PathVariable("userId") Long userId, @CurrentUser Member member) {
        if (member == null) {
            return Response.fail("미인증 회원");
        }
        logger.debug("userId로 프로필 조회 요청: {}", userId);
        Profile profile = profileService.getProfileById(userId);
        if (profile == null) {
            return Response.fail("프로필을 찾을 수 없습니다.");
        }
        logger.debug("조회된 프로필: {}", profile);
        ProfileResponseDto profileResponseDto = convertToResponseDto(profile);
        return Response.success("다른 사용자 프로필 조회 성공", profileResponseDto);
    }

    @PostMapping
    public Response<ProfileResponseDto> createProfile(@RequestBody ProfileDto profileDto) {
        logger.debug("프로필 생성 요청: {}", profileDto);
        Member member = memberService.getMemberById(profileDto.getUserId());
        logger.debug("조회된 회원: {}", member);
        if (member == null) {
            throw new IllegalArgumentException("유효하지 않은 userId " + profileDto.getUserId());
        }

        Profile profile = convertFromDtoToEntity(profileDto);
        profile.setMember(member);
        Profile savedProfile = profileService.saveProfile(profile);
        logger.debug("생성된 프로필: {}", savedProfile);
        ProfileResponseDto profileResponseDto = convertToResponseDto(savedProfile);
        return Response.success("프로필 생성 완료", profileResponseDto);
    }


    /*
    @PutMapping("/{userId}")
    public Response<ProfileResponseDto> createOrUpdateProfile( @RequestBody ProfileDto profileDto, @CurrentUser Member member) {
        logger.debug("userId와 profileDto로 프로필 생성 또는 수정 요청: {}와 {}", member.getUserId(), profileDto);

        logger.debug("조회된 회원: {}", member);
        if (member == null) {
            throw new IllegalArgumentException("유효하지 않은 userId " + member.getUserId());
        }

        validateAndSetDefaults(profileDto);

        Profile profile = convertFromDtoToEntity(profileDto);
        profile.setUserId(member);

        Profile existingProfile = profileService.getProfileById(member.getUserId());
        Profile savedProfile;
        if (existingProfile == null) {
            logger.debug("기존 프로필이 없어 새로 생성합니다.");
            savedProfile = profileService.saveProfile(profile);
        } else {
            logger.debug("기존 프로필을 업데이트합니다.");
            savedProfile = profileService.updateProfile(profileDto, member); // DTO를 전달하도록 수정
        }
        logger.debug("생성 또는 수정된 프로필: {}", savedProfile);

        ProfileResponseDto profileResponseDto = convertToResponseDto(savedProfile);
        return Response.success("프로필 최초 수정 완료", profileResponseDto);
    }

    private void validateAndSetDefaults(ProfileDto profileDto) {
        if (profileDto.getSns() == null) {
            throw new IllegalArgumentException("sns은 null이 될 수 없습니다.");
        }
        if (profileDto.getProfileImageUrl() == null) {
            profileDto.setProfileImageUrl("");
        }
        if (profileDto.getBio() == null) {
            profileDto.setBio("");
        }
        if (profileDto.getSelfIntro() == null) {
            profileDto.setSelfIntro("");
        }
        if (profileDto.getSkills() == null) {
            profileDto.setSkills(new HashMap<>());
        }
    }

     */


    @PatchMapping("/upload")
    public Response<ProfileResponseDto> updateProfile(@CurrentUser Member member, @RequestBody ProfileDto profileDto) {
        logger.debug("userId와 profileDto로 프로필 수정 요청: {}와 {}", member.getUserId(), profileDto);

        logger.debug("조회된 회원: {}", member);
        if (member == null) {
            throw new IllegalArgumentException("유효하지 않은 userId " + profileDto.getUserId());
        }

        profileDto.setUserId(member.getUserId());
        Profile updatedProfile = profileService.updateProfile(profileDto, member);
        logger.debug("수정된 프로필: {}", updatedProfile);
        ProfileResponseDto profileResponseDto = convertToResponseDto(updatedProfile);
        return Response.success("프로필 수정 완료", profileResponseDto);
    }


    @DeleteMapping
    public void deleteProfile(@CurrentUser Member member) {
        logger.debug("userId로 프로필 삭제 요청: {}", member.getUserId());
        profileService.deleteProfileById(member.getUserId());
        logger.debug("삭제된 프로필 userId: {}", member.getUserId());
    }

    /*
    private Profile convertFromRequestDtoToEntity(ProfileRequestDto profileRequestDto) {
        Member member = memberService.getMemberById(profileRequestDto.getUserId());
        logger.debug("ProfileRequestDto를 Profile 엔티티로 변환: {}", member);
        return Profile.builder()
                .userId(member)
                .nickname(profileRequestDto.getNickname())
                .age(profileRequestDto.getAge())
                .build();
    }

     */

    private Profile convertFromDtoToEntity(ProfileDto profileDto) {
        Member member = memberService.getMemberById(profileDto.getUserId());
        logger.debug("ProfileDto를 Profile 엔티티로 변환: {}", member);
        return Profile.builder()
                .profileId(profileDto.getProfileId())
                .member(member)
                .name(profileDto.getName())
                .nickname(profileDto.getNickname())
                .age(profileDto.getAge())
                .sns(profileDto.getSns())
                .profileImageUrl(profileDto.getProfileImageUrl())
                .bio(profileDto.getBio())
                .selfIntro(profileDto.getSelfIntro())
                .skills(profileDto.getSkills())
                .mannerScore(profileDto.getMannerScore())
                .mannerTier(profileDto.getMannerTier())
                .build();
    }

    private ProfileResponseDto convertToResponseDto(Profile profile) {
        return new ProfileResponseDto(
                profile.getProfileId(),
                profile.getMember().getUserId(),
                profile.getMannerTier(),
                profile.getMannerScore(),
                profile.getName(),
                profile.getNickname(),
                profile.getAge(),
                profile.getSns(),
                profile.getProfileImageUrl(),
                profile.getBio(),
                profile.getSelfIntro(),
                profile.getSkills()
        );
    }
}
