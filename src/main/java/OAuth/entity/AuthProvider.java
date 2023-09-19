package OAuth.entity;

import java.util.Arrays;

public enum AuthProvider {
    GOOGLE("google"),
    KAKAO("kakao"),
    EMPTY("");

    private String authProvider;

    public String getAuthProvider() {
        return authProvider;
    }
    AuthProvider(String authProvider){
        this.authProvider = authProvider;
    }

    // claim 에서 추출한 provider code 로 enum 값 찾기
    public static AuthProvider findByCode(String code){
        return Arrays.stream(AuthProvider.values())
                .filter(provider -> provider.getAuthProvider().equals(code))
                .findFirst()
                .orElse(EMPTY);
    }
}
