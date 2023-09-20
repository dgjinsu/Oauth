package OAuth.security;

import OAuth.entity.AuthProvider;
import OAuth.exception.CustomException;
import OAuth.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("**** SECURITY FILTER START");
        try {
            String authorizationHeader = request.getHeader("Authorization");
            String token;
            String userId;
            String provider;

            // 헤더가 null 이 아니고 올바른 토큰이라면
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                // 토큰 추출
                token = authorizationHeader.substring(7);

                // 만료 체크
                if (jwtProvider.isExpiration(token)) {
                    throw new CustomException("EXPIRED_ACCESS_TOKEN");
                }

                // claim 을 받아와 정보 추출
                userId = (String) jwtProvider.get(token).get("userId");
                provider = (String) jwtProvider.get(token).get("provider");

                // DB 에 정보가 있는지 확인
                if(!userRepository.existsByIdAndAuthProvider(userId, AuthProvider.findByCode(provider))){
                    throw new CustomException("CANNOT_FOUND_USER");
                }

                // 인증 정보 생성
                Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, null);
                // SecurityContextHolder에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } catch (CustomException e) {
            // 만료된 토큰 에러라면
            if (e.getMessage().equalsIgnoreCase("EXPIRED_ACCESS_TOKEN")) {
                writeErrorLogs("EXPIRED_ACCESS_TOKEN", e.getMessage(), e.getStackTrace());
                JSONObject jsonObject = createJsonError(String.valueOf(UNAUTHORIZED.value()), e.getMessage());
                setJsonResponse(response, UNAUTHORIZED, jsonObject.toString());
            }
            // DB 에 없는 유저라면
            else if (e.getMessage().equalsIgnoreCase("CANNOT_FOUND_USER")) {
                writeErrorLogs("CANNOT_FOUND_USER", e.getMessage(), e.getStackTrace());
                JSONObject jsonObject = createJsonError(String.valueOf(UNAUTHORIZED.value()), e.getMessage());
                setJsonResponse(response, UNAUTHORIZED, jsonObject.toString());
            }
        } catch (Exception e) {
            writeErrorLogs("Exception", e.getMessage(), e.getStackTrace());

            if (response.getStatus() == HttpStatus.OK.value()) {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        } finally {
            log.debug("**** SECURITY FILTER FINISH");
        }
    }

    // 에러 content
    private JSONObject createJsonError(String errorCode, String errorMessage) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("error_code", errorCode);
            jsonObject.put("error_message", errorMessage);
        } catch (Exception ex) {
            writeErrorLogs("JSONException", ex.getMessage(), ex.getStackTrace());
        }

        return jsonObject;
    }

    // 에러 응답 반환
    private void setJsonResponse(HttpServletResponse response, HttpStatus httpStatus, String jsonValue) {
        response.setStatus(httpStatus.value());
        response.setContentType(APPLICATION_JSON_VALUE);

        try {
            response.getWriter().write(jsonValue);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException ex) {
            writeErrorLogs("IOException", ex.getMessage(), ex.getStackTrace());
        }
    }

    private void writeErrorLogs(String exception, String message, StackTraceElement[] stackTraceElements) {
        log.error("**** " + exception + " ****");
        log.error("**** error message : " + message);
        log.error("**** stack trace : " + Arrays.toString(stackTraceElements));
    }
}
