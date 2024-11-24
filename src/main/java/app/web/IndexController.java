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
public class IndexController {

    public static final String GET_HOME_REDIRECT = "redirect:/home";
    public static final String GET_INDEX_REDIRECT = "redirect:/";
    public static final String GET_INDEX_VIEW = "index";

    private final SessionManager sessionManager;
    private final UserService userService;

    public IndexController(SessionManager sessionManager, UserService userService) {
        this.sessionManager = sessionManager;
        this.userService = userService;
    }

    @GetMapping()
    public String getIndex() {

        if (sessionManager.hasActiveSession()) {
            return GET_HOME_REDIRECT;
        }

        return GET_INDEX_VIEW;
    }

    @GetMapping("/home")
    public ModelAndView getHome() {

        if (!sessionManager.hasActiveSession()) {
            return new ModelAndView(GET_INDEX_REDIRECT);
        }

        User user = userService.getById(sessionManager.getUserId());
        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/login")
    public ModelAndView getLogin() {

        if (sessionManager.hasActiveSession()) {
            return new ModelAndView(GET_HOME_REDIRECT);
        }

        ModelAndView modelAndView = new ModelAndView("login");
        modelAndView.addObject("loginRequest", new LoginRequest());

        return modelAndView;
    }

    @GetMapping("/register")
    public ModelAndView getRegister() {

        if (sessionManager.hasActiveSession()) {
            return new ModelAndView(GET_HOME_REDIRECT);
        }

        ModelAndView modelAndView = new ModelAndView("register");
        modelAndView.addObject("registerRequest", new RegisterRequest());

        return modelAndView;
    }

    @GetMapping("/logout")
    public String getLogout() {

        sessionManager.terminateCurrentSession();
        return GET_INDEX_REDIRECT;
    }
}
