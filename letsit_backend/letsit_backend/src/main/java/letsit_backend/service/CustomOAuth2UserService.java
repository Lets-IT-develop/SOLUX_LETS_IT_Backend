package letsit_backend.service;

import letsit_backend.dto.auth.*;
import letsit_backend.model.Member;
import letsit_backend.model.OAuth2Provider;
import letsit_backend.model.Role;
import letsit_backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    private OAuth2Response resolveOAuth2Response(OAuth2Provider provider, OAuth2User oAuth2User) {
        return switch (provider) {
            case GOOGLE -> new GoogleResponse(oAuth2User.getAttributes());
            case NAVER -> new NaverResponse(oAuth2User.getAttributes());
            case KAKAO -> new KakaoResponse(oAuth2User.getAttributes());
        };
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        //어느 서비스의 로그인인지 확인
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        OAuth2Provider provider = OAuth2Provider.from(registrationId);
        OAuth2Response oAuth2Response = resolveOAuth2Response(provider, oAuth2User);

        //리소스 서버에서 발급 받은 정보로 사용자를 특정할 아이디 값을 만듦
        String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();

        Member existData = memberRepository.findByUsername(username);

        if (existData == null) {
            existData = Member.builder()
                    .username(username)
                    .email(oAuth2Response.getEmail())
                    .name(oAuth2Response.getName())
                    .role(Role.USER)
                    .build();
        }
        else {
            existData.updateInfo(oAuth2Response.getEmail(), oAuth2Response.getName());
        }

        memberRepository.save(existData);

        MemberDto memberDto = MemberDto.builder()
                .username(username)
                .name(oAuth2Response.getName())
                .role("ROLE_USER")
                .build();

        return new CustomOAuth2User(memberDto);
    }
}
