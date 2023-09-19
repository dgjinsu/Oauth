package OAuth.dto;

import OAuth.entity.AuthProvider;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class SignInResponse {
    private AuthProvider authProvider;
    private KakaoUserInfo kakaoUserInfo;
    private String accessToken;
    private String refreshToken;
}
