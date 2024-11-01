package kz.romanb.onelabproject.security;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class JwtUtils {
    public static JwtAuthentication generate(Claims claims) {
        final JwtAuthentication jwtInfoToken = new JwtAuthentication();
        jwtInfoToken.setEmail(claims.getSubject());
        jwtInfoToken.setUsername(claims.get("username").toString());
        Set<GrantedAuthority> authorities = ((List<?>) claims.get("authorities"))
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.toString()))
                .collect(Collectors.toSet());
        jwtInfoToken.setRoles(authorities);
        return jwtInfoToken;
    }
}
