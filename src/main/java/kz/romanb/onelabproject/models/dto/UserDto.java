package kz.romanb.onelabproject.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kz.romanb.onelabproject.models.entities.Role;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Информация о пользователе")
public class UserDto {

    @Schema(description = "ID пользователя", example = "1")
    private Long id;
    @Schema(description = "Имя пользователя", example = "username")
    private String username;
    @Schema(description = "Email пользователя", example = "user@gmail.com")
    private String email;
    @Builder.Default
    @Schema(description = "Роли пользователя", allowableValues = {"USER", "ADMIN"})
    private Set<Role> roles = new HashSet<>();
}
