package OAuth.controller;

import OAuth.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@AllArgsConstructor
public class UserController {
    private final UserService userService;
}
