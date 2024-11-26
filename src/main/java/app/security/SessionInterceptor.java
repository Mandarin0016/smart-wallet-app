package app.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    public static final Set<String> UNAUTHENTICATED_ENDPOINTS = Set.of("/login", "/register", "/users/login", "/users/register", "/");
    public static final String USER_ID_FROM_SESSION = "user_id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String endpoint = request.getServletPath();
        if (UNAUTHENTICATED_ENDPOINTS.contains(endpoint)) {
            // има сесия - редирект към /home
            if (request.getSession(false) != null && request.getSession(false).getAttribute(USER_ID_FROM_SESSION) != null) {
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

        if (handler instanceof HandlerMethod handlerMethod) {

            if (handlerMethod.hasMethodAnnotation(RequiresSelfUserAction.class)) {
                UUID userIdFromSession = (UUID) request.getSession(false).getAttribute(USER_ID_FROM_SESSION);

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
        }

        return true;
    }
}
