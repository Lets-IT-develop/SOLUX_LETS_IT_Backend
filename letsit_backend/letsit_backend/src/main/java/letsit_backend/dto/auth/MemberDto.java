package letsit_backend.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberDto {
    private String role;
    private String name;
    private String username;
}
