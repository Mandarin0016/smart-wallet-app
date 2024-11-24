package app.web;

import app.security.SessionManager;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import static app.web.IndexController.GET_HOME_REDIRECT;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final SessionManager sessionManager;

    public UserController(UserService userService, SessionManager sessionManager) {
        this.userService = userService;
        this.sessionManager = sessionManager;
    }

    @PostMapping("/login")
    public ModelAndView login(@Valid LoginRequest loginRequest, BindingResult result) {

        if (result.hasErrors()) {
            return new ModelAndView("/login");
        }

        User loggedUser = userService.login(loginRequest);
        if (loggedUser == null) {
            ModelAndView modelAndView = new ModelAndView("/login");
            modelAndView.addObject("invalidCredentials", "Invalid username or password.");
            return modelAndView;
        }

        ModelAndView modelAndView = new ModelAndView(GET_HOME_REDIRECT);
        modelAndView.addObject("user", loggedUser);
        sessionManager.activeUserSession(loggedUser.getId());

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView register(@Valid RegisterRequest registerRequest, BindingResult result) {

        if (result.hasErrors()) {
            return new ModelAndView("/register");
        }

        User loggedUser = userService.register(registerRequest);
        if (loggedUser == null) {
            ModelAndView modelAndView = new ModelAndView("/register");
            modelAndView.addObject("usernameAlreadyInUse", "Username already exist.");
            return modelAndView;
        }

        ModelAndView modelAndView = new ModelAndView(GET_HOME_REDIRECT);
        modelAndView.addObject("user", loggedUser);
        sessionManager.activeUserSession(loggedUser.getId());

        return modelAndView;
    }
}