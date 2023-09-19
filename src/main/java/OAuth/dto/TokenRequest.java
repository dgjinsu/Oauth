package OAuth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TokenRequest {
    private String registrationId; // "google", "kakao" 등 어떤 프로바이더인지
    private String code; // 토큰을 교환하기 위해 사용
    private String state;
    private String refreshToken;

    @Builder
    public TokenRequest(String registrationId, String code, String state){
        this.registrationId = registrationId;
        this.code = code;
        this.state = state;
    }
}
