package webserver.controler;

import org.springframework.web.bind.annotation.*;
import webserver.model.ApplicationUser;
import webserver.service.UserService;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/sign-up")
    public void signUp(@RequestBody ApplicationUser applicationUser) {
        userService.signUp(applicationUser);
    }
}
