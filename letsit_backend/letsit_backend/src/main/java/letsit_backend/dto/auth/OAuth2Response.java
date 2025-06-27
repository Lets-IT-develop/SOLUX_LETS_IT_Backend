package letsit_backend.dto.auth;

public interface OAuth2Response {
    //제공 서비스
    String getProvider();
    //서비스에서 발급해주는 아이디(번호)
    String getProviderId();
    //이메일
    String getEmail();
    //사용자 실명 (설정한 이름)
    String getName();
}
