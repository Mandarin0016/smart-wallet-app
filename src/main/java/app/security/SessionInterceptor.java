package app.security;

import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class SessionInterceptor implements HandlerInterceptor {

    public static final Set<String> UNAUTHENTICATED_ENDPOINTS = Set.of("/login", "/register", "/users/login",
            "/users/register", "/");
    public static final String USER_ID_FROM_SESSION = "user_id";

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String endpoint = request.getServletPath();
        if (UNAUTHENTICATED_ENDPOINTS.contains(endpoint)) {
            // има сесия - редирект към /home
            if (request.getSession(false) != null
                    && request.getSession(false).getAttribute(USER_ID_FROM_SESSION) != null) {
                response.sendRedirect("/home");
                return false;
            }
            // няма сесия - пусни напред
            return true;
        }

        if (request.getSession(false) == null || request.getSession(false).getAttribute(USER_ID_FROM_SESSION) == null) {
            response.sendRedirect("/");
            return false;
        }

        HttpSession session = request.getSession(false);
        UUID userIdFromSession = (UUID) session.getAttribute(USER_ID_FROM_SESSION);
        User user = userService.getById(userIdFromSession);

        if (!user.isActive()) {
            session.invalidate();
            response.sendRedirect("/");
            return false;
        }

        if (handler instanceof HandlerMethod handlerMethod) {

            if (handlerMethod.hasMethodAnnotation(RequiresSelfUserAction.class)) {

                Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                String userIdFromPathVariable = pathVariables.get("userId");

                if (userIdFromPathVariable == null) {
                    response.setStatus(HttpStatus.BAD_REQUEST.value());
                    response.getWriter().write("Expected 'userId' parameter is missing in the request.");
                    return false;
                }

                if (!userIdFromSession.equals(UUID.fromString(userIdFromPathVariable))) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.getWriter().write("Access denied. User ID mismatch.");
                    return false;
                }
            }

            if (handlerMethod.hasMethodAnnotation(RequireAdminRole.class) && user.getRole() != UserRole.ADMIN) {

                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.getWriter().write("Access denied, required permissions are missing.");
                return false;
            }
        }

        return true;
    }
}
