package letsit_backend.dto.profile;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SNSRequestDto {
    private Map<String, String> sns; // sns는 그냥 String으로 저장?
}
