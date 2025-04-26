package letsit_backend.dto.auth;

import lombok.Data;

@Data
public class KakaoMemberDto {
    private Long id;
    private KakaoMember kakao_member;

    @Data
    public static class KakaoMember {
        //private String email;
        private KakaoProfile profile;

        @Data
        public static class KakaoProfile {
            private String name;
            private String age_range;
            private String gender;
            private String profile_image;
        }
    }
}
