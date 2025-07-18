package letsit_backend.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import letsit_backend.CurrentUser;
import letsit_backend.dto.Response;
import letsit_backend.dto.auth.CustomOAuth2User;
import letsit_backend.dto.profile.ProfileRequestDto;
import letsit_backend.dto.profile.ProfileResponseDto;
import letsit_backend.dto.profile.ProfileUpdateRequestDto;
import letsit_backend.dto.profile.SNSRequestDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "프로필 관련 기능", description = "/profile 로 시작하는 API들입니다.")
public interface ProfileApi {

    @Operation(summary = "프로필 조회", description = "내 프로필 조회 기능입니다.")
    Response<ProfileResponseDto> getMyProfile(@Parameter(hidden = true) @CurrentUser CustomOAuth2User oAuth2User);

    @Operation(summary = "타인 프로필 조회", description = "특정 유저의 프로필을 조회합니다.")
    @Parameter(name = "userId", description = "사용자 ID", example = "1")
    Response<ProfileResponseDto> getUserProfile(@PathVariable("userId") Long userId);

    @Operation(summary = "프로필 생성", description = "로그인하자마자 프로필을 생성합니다.")
    Response<String> createProfile(@RequestBody ProfileRequestDto profileRequestDto, @Parameter(hidden = true) @CurrentUser CustomOAuth2User oAuth2User);

    @Operation(summary = "프로필 업데이트", description = "프로필을 업데이트 합니다.")
    Response<String> updateProfile(@RequestBody ProfileUpdateRequestDto profileUpdateRequestDto, @Parameter(hidden = true) @CurrentUser CustomOAuth2User oAuth2User);

    @Operation(summary = "sns 등록", description = "프로필에 sns를 추가합니다.")
    Response<String> createSNS(@RequestBody SNSRequestDto snsRequestDto, @Parameter(hidden = true) @CurrentUser CustomOAuth2User oAuth2User);
}
