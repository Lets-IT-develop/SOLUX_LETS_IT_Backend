package letsit_backend.model;

public enum OAuth2Provider {
    GOOGLE, NAVER, KAKAO;

    public static OAuth2Provider from(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> GOOGLE;
            case "naver" -> NAVER;
            case "kakao" -> KAKAO;
            default -> throw new IllegalArgumentException("지원하지 않는 OAuth Provider입니다: " + registrationId);
        };
    }
}
