package app.web.dto;

import app.user.model.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserEditRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String profilePicture;

    public static UserEditRequest buildFromUser(User user) {

        return UserEditRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .build();
    }
}
