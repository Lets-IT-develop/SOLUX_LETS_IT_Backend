package letsit_backend.controller;

import letsit_backend.CurrentUser;
import letsit_backend.dto.Response;
import letsit_backend.dto.auth.CustomOAuth2User;
import letsit_backend.dto.profile.ProfileRequestDto;
import letsit_backend.dto.profile.ProfileResponseDto;
import letsit_backend.dto.profile.ProfileUpdateRequestDto;
import letsit_backend.dto.profile.SNSRequestDto;
import letsit_backend.service.ProfileService;
import letsit_backend.swagger.ProfileApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
public class ProfileController implements ProfileApi {

    @Autowired
    private ProfileService profileService;

    @GetMapping
    public Response<ProfileResponseDto> getMyProfile(@CurrentUser CustomOAuth2User oAuth2user) {
        ProfileResponseDto profileResponseDto = profileService.getProfileInfo(oAuth2user.getId());
        return Response.success("내 프로필 조회 성공", profileResponseDto);
    }

    @GetMapping("/{userId}")
    public Response<ProfileResponseDto> getUserProfile(@PathVariable Long userId) {
        ProfileResponseDto profileResponseDto = profileService.getProfileInfo(userId);
        return Response.success("다른 사용자 프로필 조회 성공", profileResponseDto);
    }

    @PostMapping
    public Response<String> createProfile(@RequestBody ProfileRequestDto profileRequestDto, @CurrentUser CustomOAuth2User oAuth2User) {
        profileService.createProfile(oAuth2User.getId(), profileRequestDto);

        return Response.success("프로필 설정 완료", null);
    }

    @PutMapping
    public Response<String> updateProfile(@RequestBody ProfileUpdateRequestDto profileUpdateRequestDto, @CurrentUser CustomOAuth2User oAuth2User) {

        profileService.updateProfile(oAuth2User.getId(), profileUpdateRequestDto);

        return Response.success("프로필 수정 완료", null);
    }

    @PutMapping("/sns")
    public Response<String> createSNS(@RequestBody SNSRequestDto snsRequestDto, @CurrentUser CustomOAuth2User oAuth2User) {

        profileService.createSNS(oAuth2User.getId(), snsRequestDto);

        return Response.success("sns 등록 완료", null);
    }
}
