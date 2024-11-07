package kz.romanb.onelabproject.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import kz.romanb.onelabproject.exceptions.AuthException;
import kz.romanb.onelabproject.models.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
@Slf4j
public class JwtProvider {
    private final SecretKey accessSecret;
    private final SecretKey refreshSecret;
    private final long expirationAccessTokenInMinutes;
    private final long expirationRefreshTokenInDays;

    public JwtProvider(@Value("${jwt.secret.access}") String accessSecret,
                       @Value("${jwt.secret.refresh}") String refreshSecret,
                       @Value("${jwt.expiration.access}") long expirationAccessTokenInMinutes,
                       @Value("${jwt.expiration.refresh}") long expirationRefreshTokenInDays) {
        this.accessSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
        this.refreshSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
        this.expirationAccessTokenInMinutes = expirationAccessTokenInMinutes;
        this.expirationRefreshTokenInDays = expirationRefreshTokenInDays;
    }

    public String generateAccessToken(User user) {
        final LocalDateTime now = LocalDateTime.now();
        final Instant accessExpirationInstance = now.plusMinutes(expirationAccessTokenInMinutes)
                .atZone(ZoneId.systemDefault()).toInstant();
        final Date accessExpiration = Date.from(accessExpirationInstance);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setExpiration(accessExpiration)
                .setIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(accessSecret)
                .claim("email", user.getEmail())
                .claim("username", user.getUsername())
                .claim("authorities", user.getAuthorities())
                .compact();
    }

    public String generateRefreshToken(User user) {
        final LocalDateTime now = LocalDateTime.now();
        final Instant refreshExpirationInstance = now.plusDays(expirationRefreshTokenInDays)
                .atZone(ZoneId.systemDefault()).toInstant();
        final Date refreshExpiration = Date.from(refreshExpirationInstance);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setExpiration(refreshExpiration)
                .setIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(refreshSecret)
                .compact();
    }

    public Claims getAccessClaims(String accessToken) {
        return getClaims(accessToken, accessSecret);
    }

    public Claims getRefreshClaims(String refreshToken) {
        return getClaims(refreshToken, refreshSecret);
    }

    private Claims getClaims(String token, Key secret) {
        return Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateAccessToken(String accessToken) {
        return validateToken(accessToken, accessSecret);
    }

    public boolean validateRefreshToken(String refreshToken) {
        return validateToken(refreshToken, refreshSecret);
    }

    private boolean validateToken(String token, Key secret) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException expEx) {
            log.error("Истек срок действия токена");
            throw new AuthException("Истек срок действия токена");
        } catch (UnsupportedJwtException unsEx) {
            log.error("Неподдерживаемый JWT", unsEx);
            throw new AuthException("Неподдерживаемый JWT");
        } catch (MalformedJwtException malEx) {
            log.error("Некорректный JWT", malEx);
            throw new AuthException("Некорректный JWT");
        } catch (SignatureException sEx) {
            log.error("Недействительная подпись");
            throw new AuthException("Недействительная подпись");
        }

    }
}
