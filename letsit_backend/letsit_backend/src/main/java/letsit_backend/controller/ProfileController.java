package letsit_backend.controller;

import letsit_backend.CurrentUser;
import letsit_backend.dto.Response;
import letsit_backend.dto.profile.ProfileRequestDto;
import letsit_backend.dto.profile.ProfileResponseDto;
import letsit_backend.dto.profile.ProfileUpdateRequestDto;
import letsit_backend.dto.profile.SNSRequestDto;
import letsit_backend.model.Member;
import letsit_backend.service.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @Autowired
    private ProfileService profileService;

    @GetMapping
    public Response<ProfileResponseDto> getMyProfile(@CurrentUser Member member) {
        ProfileResponseDto profileResponseDto = profileService.getProfileInfo(member.getUserId());
        return Response.success("내 프로필 조회 성공", profileResponseDto);
    }

    @GetMapping("/{userId}")
    public Response<ProfileResponseDto> getUserProfile(@PathVariable Long userId) {
        ProfileResponseDto profileResponseDto = profileService.getProfileInfo(userId);
        return Response.success("다른 사용자 프로필 조회 성공", profileResponseDto);
    }

    @PostMapping
    public Response<String> createProfile(@RequestBody ProfileRequestDto profileRequestDto, @CurrentUser Member member) {
        Long userId = member.getUserId();

        profileService.createProfile(userId, profileRequestDto);

        return Response.success("프로필 설정 완료", null);
    }

    @PutMapping
    public Response<String> updateProfile(@RequestBody ProfileUpdateRequestDto profileUpdateRequestDto, @CurrentUser Member member) {
        Long userId = member.getUserId();

        profileService.updateProfile(userId, profileUpdateRequestDto);

        return Response.success("프로필 수정 완료", null);
    }

    @PutMapping("/sns")
    public Response<String> createSNS(@RequestBody SNSRequestDto snsRequestDto, @CurrentUser Member member) {
        Long userId = member.getUserId();

        profileService.createSNS(userId, snsRequestDto);

        return Response.success("sns 등록 완료", null);
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
