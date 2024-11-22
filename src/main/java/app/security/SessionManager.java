package app.security;

import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Component
public class SessionManager {

    private UUID userId;
    private LocalDateTime loggedAt;
    private LocalDateTime expireAt;
    private final HttpSession session;

    public SessionManager(HttpSession session) {
        this.session = session;
    }

    public boolean hasActiveSession() {

        if (userId == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(expireAt)) {
            terminateCurrentSession();
            return false;
        }

        return true;
    }

    public void activeUserSession(UUID userId) {
        this.userId = userId;
        this.loggedAt = LocalDateTime.now();
        this.expireAt = loggedAt.plusMinutes(5);
    }

    public void terminateCurrentSession() {
        this.userId = null;
        this.loggedAt = null;
        this.expireAt = null;
    }
}
