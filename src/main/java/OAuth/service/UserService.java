package OAuth.service;

import OAuth.dto.SignUpRequest;
import OAuth.entity.Role;
import OAuth.entity.User;
import OAuth.exception.CustomException;
import OAuth.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public Long createUser(SignUpRequest signUpRequest) {
        if(userRepository.existsByIdAndAuthProvider(signUpRequest.getId(), signUpRequest.getAuthProvider())){
            throw new CustomException("already exist user");
        }

        return userRepository.save(
                User.builder()
                        .id(signUpRequest.getId())
                        .nickname(signUpRequest.getNickname())
                        .email(signUpRequest.getEmail())
                        .profileImageUrl(signUpRequest.getProfileImageUrl())
                        .role(Role.MEMBER)
                        .authProvider(signUpRequest.getAuthProvider())
                        .build()).getId();
    }
}
