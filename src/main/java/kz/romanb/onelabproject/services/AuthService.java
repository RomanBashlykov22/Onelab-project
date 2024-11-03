package kz.romanb.onelabproject.services;

import io.jsonwebtoken.Claims;
import kz.romanb.onelabproject.exceptions.AuthException;
import kz.romanb.onelabproject.models.dto.JwtRequest;
import kz.romanb.onelabproject.models.dto.JwtResponse;
import kz.romanb.onelabproject.models.entities.AccessToken;
import kz.romanb.onelabproject.models.entities.RefreshToken;
import kz.romanb.onelabproject.models.entities.User;
import kz.romanb.onelabproject.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserService userService;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public JwtResponse login(JwtRequest jwtRequest) {
        User user = (User) userService.loadUserByUsername(jwtRequest.email());

        if (passwordEncoder.matches(jwtRequest.password(), user.getPassword())) {
            String accessTokenStr = jwtProvider.generateAccessToken(user);
            AccessToken accessToken = AccessToken.builder()
                    .user(user)
                    .accessToken(accessTokenStr)
                    .createdAt(jwtProvider.getAccessClaims(accessTokenStr).getIssuedAt())
                    .expiresAt(jwtProvider.getAccessClaims(accessTokenStr).getExpiration())
                    .build();

            String refreshTokenStr = jwtProvider.generateRefreshToken(user);
            RefreshToken refreshToken = RefreshToken.builder()
                    .user(user)
                    .refreshToken(refreshTokenStr)
                    .createdAt(jwtProvider.getRefreshClaims(refreshTokenStr).getIssuedAt())
                    .expiresAt(jwtProvider.getRefreshClaims(refreshTokenStr).getExpiration())
                    .build();

            if (accessTokenService.findTokenByUser(user).isPresent()) {
                accessTokenService.updateToken(accessToken);
            } else {
                accessTokenService.saveToken(accessToken);
            }

            if (refreshTokenService.findTokenByUser(user).isPresent()) {
                refreshTokenService.updateToken(refreshToken);
            } else {
                refreshTokenService.saveToken(refreshToken);
            }

            return new JwtResponse(accessTokenStr, refreshTokenStr);
        } else {
            throw new AuthException("Неправильный пароль");
        }
    }

    public JwtResponse getNewAccessToken(String refreshToken) {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            String email = claims.getSubject();

            User user = (User) userService.loadUserByUsername(email);
            Optional<RefreshToken> saveRefreshTokenOptional = refreshTokenService.findTokenByUser(user);

            if (saveRefreshTokenOptional.isPresent() && saveRefreshTokenOptional.get().getRefreshToken().equals(refreshToken)) {
                String newAccessTokenStr = jwtProvider.generateAccessToken(user);
                AccessToken newAccessToken = AccessToken.builder()
                        .user(user)
                        .accessToken(newAccessTokenStr)
                        .createdAt(jwtProvider.getAccessClaims(newAccessTokenStr).getIssuedAt())
                        .expiresAt(jwtProvider.getAccessClaims(newAccessTokenStr).getExpiration())
                        .build();
                accessTokenService.updateToken(newAccessToken);
                return new JwtResponse(newAccessTokenStr, null);
            }
        }
        throw new AuthException("Неверный JWT токен");
    }

    public JwtResponse getNewRefreshToken(String refreshToken) {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            String email = claims.getSubject();

            User user = (User) userService.loadUserByUsername(email);
            Optional<RefreshToken> saveRefreshTokenOptional = refreshTokenService.findTokenByUser(user);

            if (saveRefreshTokenOptional.isPresent() && saveRefreshTokenOptional.get().getRefreshToken().equals(refreshToken)) {
                String newAccessTokenStr = jwtProvider.generateAccessToken(user);
                AccessToken newAccessToken = AccessToken.builder()
                        .user(user)
                        .accessToken(newAccessTokenStr)
                        .createdAt(jwtProvider.getAccessClaims(newAccessTokenStr).getIssuedAt())
                        .expiresAt(jwtProvider.getAccessClaims(newAccessTokenStr).getExpiration())
                        .build();

                String newRefreshTokenStr = jwtProvider.generateRefreshToken(user);
                RefreshToken newRefreshToken = RefreshToken.builder()
                        .user(user)
                        .refreshToken(newRefreshTokenStr)
                        .createdAt(jwtProvider.getRefreshClaims(newRefreshTokenStr).getIssuedAt())
                        .expiresAt(jwtProvider.getRefreshClaims(newRefreshTokenStr).getExpiration())
                        .build();

                accessTokenService.updateToken(newAccessToken);
                refreshTokenService.updateToken(newRefreshToken);

                return new JwtResponse(newAccessTokenStr, newRefreshTokenStr);
            }
        }
        throw new AuthException("Неверный JWT токен");
    }

    public boolean logout(String refreshToken) {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            String email = claims.getSubject();

            User user = (User) userService.loadUserByUsername(email);
            refreshTokenService.deleteToken(user);
            accessTokenService.deleteToken(user);

            return true;
        }
        return false;
    }
}
