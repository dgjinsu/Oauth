package OAuth.controller;

import OAuth.dto.SignUpRequest;
import OAuth.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@Slf4j
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/user")
    public ResponseEntity<Long> createUser(@RequestBody SignUpRequest signUpRequest){
        return ResponseEntity.ok(userService.createUser(signUpRequest));
    }
}
