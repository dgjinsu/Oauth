package OAuth.dto;

import OAuth.entity.AuthProvider;
import lombok.Getter;

@Getter
public class SignUpRequest {
    private Long id;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private AuthProvider authProvider;
}
