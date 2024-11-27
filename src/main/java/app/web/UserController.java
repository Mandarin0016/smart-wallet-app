package app.web;

import app.security.RequiresSelfUserAction;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import app.web.dto.UserEditRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

import static app.security.SessionInterceptor.USER_ID_FROM_SESSION;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ModelAndView login(@Valid LoginRequest loginRequest, BindingResult result, HttpSession session) {

        if (result.hasErrors()) {
            return new ModelAndView("login");
        }

        User loggedUser = userService.login(loginRequest);
        activateUserSession(session, loggedUser.getId());

        return getHomePageForUser(loggedUser);
    }

    @PostMapping("/register")
    public ModelAndView register(@Valid RegisterRequest registerRequest, BindingResult result, HttpSession session) {

        if (result.hasErrors()) {
            return new ModelAndView("register");
        }

        User registeredUser = userService.register(registerRequest);
        activateUserSession(session, registeredUser.getId());

        return getHomePageForUser(registeredUser);
    }

    private void activateUserSession(HttpSession session, UUID userId) {

        session.setAttribute(USER_ID_FROM_SESSION, userId);
    }

    private static ModelAndView getHomePageForUser(User loggedUser) {

        ModelAndView modelAndView = new ModelAndView("redirect:/home");
        modelAndView.addObject("user", loggedUser);

        return modelAndView;
    }

    @RequiresSelfUserAction
    @GetMapping("/{userId}/edit-profile-menu")
    public ModelAndView getProfileMenu(@PathVariable UUID userId) {

        User user = userService.getById(userId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("userEditRequest", UserEditRequest.buildFromUser(user));
        modelAndView.setViewName("profile-menu");

        return modelAndView;
    }

    @RequiresSelfUserAction
    @PostMapping("/{userId}/edit-profile-menu")
    public ModelAndView submitEditProfileMenu(@PathVariable UUID userId, @Valid UserEditRequest userEditRequest) {

        User updatedUser = userService.editUser(userId, userEditRequest);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", updatedUser);
        modelAndView.setViewName("redirect:/home");
        return modelAndView;
    }
}