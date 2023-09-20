package OAuth.service;

import OAuth.dto.KakaoUserInfo;
import OAuth.dto.SignInResponse;
import OAuth.dto.TokenRequest;
import OAuth.dto.TokenResponse;
import OAuth.entity.AuthProvider;
import OAuth.repository.UserRepository;
import OAuth.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoRequestService{
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final WebClient webClient;

    @Value("${spring.security.oauth2.client.registration.kakao.authorization-grant-type}")
    private String GRANT_TYPE;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String CLIENT_ID;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String REDIRECT_URI; // http://localhost:8080/login/oauth2/code/kakao

    @Value("${spring.security.oauth2.client.provider.kakao.token_uri}")
    private String TOKEN_URI; // https://kauth.kakao.com/oauth/token


    public SignInResponse redirect(TokenRequest tokenRequest) {
        TokenResponse tokenResponse = getToken(tokenRequest); // WebClient 호출, 카카오에 accessToken 요청
        KakaoUserInfo kakaoUserInfo = getUserInfo(tokenResponse.getAccessToken()); // 유저 resource 요청
        boolean a = userRepository.existsById(String.valueOf(kakaoUserInfo.getId()));

        // kakaoUserId 로 DB 에서 조회
        if(a){
            String accessToken = jwtProvider.createAccessToken(
                    String.valueOf(kakaoUserInfo.getId()), AuthProvider.KAKAO, tokenResponse.getAccessToken());
            String refreshToken = jwtProvider.createRefreshToken(
                    String.valueOf(kakaoUserInfo.getId()), AuthProvider.KAKAO, tokenResponse.getRefreshToken());

            // 로그인 완료 시 토큰과 함께 반환
            return SignInResponse.builder()
                    .authProvider(AuthProvider.KAKAO)
                    .kakaoUserInfo(null)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        }
        // 회원이 아닐 경우 카카오 유저 정보만 반환
        else {
            return SignInResponse.builder()
                    .authProvider(AuthProvider.KAKAO)
                    .kakaoUserInfo(kakaoUserInfo)
                    .build();
        }
    }

    public TokenResponse getToken(TokenRequest tokenRequest) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", GRANT_TYPE);
        formData.add("redirect_uri", REDIRECT_URI);
        formData.add("client_id", CLIENT_ID);
        formData.add("code", tokenRequest.getCode());

        return webClient.mutate()
                .baseUrl(TOKEN_URI)
                .build()
                .post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData)) //요청 본문을 설정
                .retrieve() // 요청을 보내고, 서버로부터 응답 받음. Mono 형태의 응답
//                .onStatus(HttpStatus::is4xxClientError, response -> Mono.just(new BadRequestException()))
                .bodyToMono(TokenResponse.class) // TokenResponse 클래스로 변환
                .block(); // 비동기적 요청 후, 응답을 블로킹하여 대기. 최종적으로 TokenResponse 객체를 반환
    }

    public KakaoUserInfo getUserInfo(String accessToken) {
        return webClient.mutate()
                .baseUrl("https://kapi.kakao.com")
                .build()
                .get()
                .uri("/v2/user/me")
                .headers(h -> h.setBearerAuth(accessToken)) // 받아온 accessToken 으로 자원 요청
                .retrieve()
                .bodyToMono(KakaoUserInfo.class) // 받아온 Kakao 정보를 class로 변환
                .block();
    }

    public TokenResponse getRefreshToken(String provider, String refreshToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", CLIENT_ID);
        formData.add("refresh_token", refreshToken);

        return webClient.mutate()
                .baseUrl("https://kauth.kakao.com")
                .build()
                .post()
                .uri("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
//                .onStatus(HttpStatus::is4xxClientError, response -> Mono.just(new BadRequestException()))
                .bodyToMono(TokenResponse.class)
                .block();
    }
}
