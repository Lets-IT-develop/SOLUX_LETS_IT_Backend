package letsit_backend.dto.post;

// 한글 표현(getKorean)과, 모든 enum 에서 재사용 가능한 fromKorean 역직렬화 로직을 제공하는 인터페이스
public interface KoreanEnum {
    String getKorean();

    /**
     * @param enumType enum 클래스 타입
     * @param korean   입력된 한글 문자열
     * @param <T>      enumType이 구현한 KoreanEnum 타입
     * @return matching enum constant
     */
    @SuppressWarnings("unchecked")
    static <T extends Enum<T> & KoreanEnum> T fromKorean(Class<T> enumType, String korean) {
        if (korean == null || korean.isEmpty()) { // 입력 없을 경우
            throw new IllegalArgumentException(
                    "Empty string is not a valid value for " + enumType.getSimpleName()
            );
        }
        for (T e : enumType.getEnumConstants()) {
            if (e.getKorean().equals(korean)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown enum value: " + korean);
    }
}
