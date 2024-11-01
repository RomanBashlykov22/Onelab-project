package kz.romanb.onelabproject.models.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtRefreshRequest {

    private String refreshToken;
}
