package kz.romanb.onelabproject.models.dto;

import kz.romanb.onelabproject.models.entities.Role;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private Long id;
    private String username;
    private String email;
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
