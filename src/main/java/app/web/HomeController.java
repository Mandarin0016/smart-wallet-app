package app.web;

import app.security.SessionManager;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

    private final SessionManager sessionManager;
    private final UserService userService;

    public HomeController(SessionManager sessionManager, UserService userService) {
        this.sessionManager = sessionManager;
        this.userService = userService;
    }

    @GetMapping()
    public ModelAndView getIndex() {

        if (sessionManager.hasActiveSession()) {
            return new ModelAndView("redirect:/home");
        }

        return new ModelAndView("index");
    }

    @GetMapping("/home")
    public ModelAndView getHome() {

        if (!sessionManager.hasActiveSession()) {
            return new ModelAndView("redirect:/");
        }

        User user = userService.getById(sessionManager.getUserId());
        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/login")
    public ModelAndView getLogin() {

        if (sessionManager.hasActiveSession()) {
            return new ModelAndView("redirect:/home");
        }

        ModelAndView modelAndView = new ModelAndView("login");
        modelAndView.addObject("loginRequest", new LoginRequest());
        return modelAndView;
    }

    @GetMapping("/register")
    public ModelAndView getRegister() {

        if (sessionManager.hasActiveSession()) {
            return new ModelAndView("redirect:/home");
        }

        ModelAndView modelAndView = new ModelAndView("register");
        modelAndView.addObject("registerRequest", new RegisterRequest());

        return modelAndView;
    }

    @GetMapping("/logout")
    public ModelAndView getLogout() {

        sessionManager.terminateCurrentSession();
        return new ModelAndView("redirect:/");
    }
}
