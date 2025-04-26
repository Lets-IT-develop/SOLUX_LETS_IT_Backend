package letsit_backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.transaction.Transactional;
import letsit_backend.dto.auth.KakaoTokenDto;
import letsit_backend.dto.auth.LoginResponseDto;
import letsit_backend.exception.CustomException;
import letsit_backend.jwt.JwtProvider;
import letsit_backend.model.KakaoProfile;
import letsit_backend.model.Member;
import letsit_backend.model.Role;
import letsit_backend.repository.MemberRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Data
@Service
public class KakaoService {


    //private final MemberDao memberDao;
    private final JwtProvider jwtProvider;
    private MemberRepository memberRepository;

    @Autowired
    public KakaoService( JwtProvider jwtProvider, MemberRepository memberRepository) {
        //this.memberDao = memberDao;
        this.jwtProvider = jwtProvider;
        this.memberRepository = memberRepository;
    }

    @Value("${kakao.client.id}")
    private String clientId;

    @Value("${kakao.redirect.uri}")
    private String redirectUri;

    //컨트롤러의 kakaoservice.getkakaoaccesstoken(code)처리
    @Transactional
    public KakaoTokenDto getKakaoToken(String code) {


        HttpHeaders headers = new HttpHeaders();
        headers.add("content-type", "application/x-www-form-urlencoded;charset=utf-8");



        //http response body 객체 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        //git ignore 해야
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);


        //프론트에서 인가 코드 요청시 받은 인가 코드 값
        params.add("code", code);

        //http 헤더 바디 합치기 위해 http entity 객체 생성
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

        //카카오로부터 Access token 받아오기
        RestTemplate restTemplate = new RestTemplate();
        // log.info("sending request to kakao: {}",params);
        // System.out.println("restTemplate = " + restTemplate);
        
        ResponseEntity<String> kakaoTokenResponse = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );
        // log.info("kakao token response: {}", kakaoTokenResponse.getBody());
        // System.out.println("kakaoTokenResponse = " + kakaoTokenResponse);

        
        //JSON parsing -> kakaoTokenDto
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        KakaoTokenDto kakaoTokenDto = null;
        try {
            kakaoTokenDto = objectMapper.readValue(kakaoTokenResponse.getBody(), KakaoTokenDto.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing Kakao token response", e);
            //e.printStackTrace();
        }
        return kakaoTokenDto;



    }


    //컨트롤러의 return authservice.kakaologin(kakaotoken)처
    public ResponseEntity<LoginResponseDto> kakaoLogin(String kakaoToken) {
        // log.info("Received Kakao token: {}", kakaoToken);

        KakaoProfile kakaoProfile = findProfile(kakaoToken);


        if (kakaoProfile == null || kakaoProfile.getKakao_account() == null) {
            // KakaoProfile 또는 KakaoAccount가 null인 경우 예외 처리
            log.error("Kakao profile or account is null");

            return ResponseEntity.badRequest().build(); // 예시로 bad request 반환
        }

        // String username = String.valueOf(kakaoProfile.getId());

        // log.info("username set to : {}", username);

        Member member = Member.builder()
                .kakaoId(kakaoProfile.getId())

                .name(kakaoProfile.kakao_account.getName())
                .ageRange(kakaoProfile.kakao_account.getAge_range())
                .gender(kakaoProfile.kakao_account.getGender())
                .profileImageUrl(kakaoProfile.getKakao_account().getProfile().getProfile_image_url())

                .role(Role.USER)
                .kakaoAccessToken(kakaoToken)
                .build();


        LoginResponseDto loginResponseDto = new LoginResponseDto();


        try {
            Optional<Member> existOwner = memberRepository.findByKakaoId(member.getKakaoId());

            if (existOwner.isEmpty()) {
                // log.info("First time login for user ID: {}", member.getKakaoId());
                memberRepository.save(member);
            } else {
                member = existOwner.get();
                member.setKakaoAccessToken(kakaoToken);
                memberRepository.save(member);
            }
            loginResponseDto.setLoginSuccess(true);
            loginResponseDto.setMember(member);

            return ResponseEntity.ok().body(loginResponseDto);

        } catch (Exception e) {
            log.error("Error during Kakao login", e);
            loginResponseDto.setLoginSuccess(false);
            return ResponseEntity.badRequest().body(loginResponseDto);
        }
    }


    public KakaoProfile findProfile(String kakaoToken) {
        // log.info("Fetching Kakao profile with token: {}", kakaoToken);
        //System.out.println("kakaoToken = " + kakaoToken);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + kakaoToken); //(1-4)
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> accountInfoRequest =
                new HttpEntity<>(headers);

        // Http 요청 (POST 방식) 후, response 변수에 응답을 받음
        ResponseEntity<String> accountInfoResponse = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                accountInfoRequest,
                String.class
        );
        // log.info("Kakao profile response: {}", accountInfoResponse.getBody());
        //System.out.println("accountInfoResponse = " + accountInfoResponse);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        KakaoProfile kakaoProfile = null;
        try {
            //log.info("Parsing Kakao profile response");

            kakaoProfile = objectMapper.readValue(accountInfoResponse.getBody(), KakaoProfile.class);
            //log.info("Parsed Kakao profile: {}", kakaoProfile);

        } catch (JsonProcessingException e) {
            log.error("Error parsing Kakao profile response", e);
            //e.printStackTrace();
        }
        return kakaoProfile;
    }
    public boolean kakaoLogout(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "https://kapi.kakao.com/v1/user/logout",
                HttpMethod.POST,
                entity,
                String.class
        );
        if (response.getStatusCode() == HttpStatus.OK) {
            log.info("Successfully logged out from Kakao");
            return true;
        } else {
            log.error("Failed to logout from Kakao: " + response.getBody());
            return false;
        }

    }

    public String getAccessToken(Long kakaoId) {
        Member member = memberRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new UsernameNotFoundException("user not found with username" + kakaoId));
        String accessToken = member.getKakaoAccessToken();
        // log.info("Retrieved access token for kakaoId {}: {}", kakaoId, accessToken);

        return accessToken;
    }


    public String getMemberByLogin(Member member) throws CustomException {

        Member loginMember = memberRepository.findByKakaoId(member.getKakaoId())
                .orElseThrow(()-> new CustomException("Invalid login information"));
        return jwtProvider.createToken(loginMember);

    }



}

