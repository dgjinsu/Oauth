package OAuth.service;

import OAuth.dto.SignInResponse;
import OAuth.dto.TokenRequest;
import OAuth.entity.AuthProvider;
import OAuth.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final KakaoRequestService kakaoRequestService;

    public SignInResponse redirect(TokenRequest tokenRequest){
        if(AuthProvider.KAKAO.getAuthProvider().equals(tokenRequest.getRegistrationId())){
            return kakaoRequestService.redirect(tokenRequest);
        }

        throw new CustomException("not supported oauth provider");
    }
}
