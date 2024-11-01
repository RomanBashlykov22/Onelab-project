package kz.romanb.onelabproject.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import kz.romanb.onelabproject.exceptions.AuthException;
import kz.romanb.onelabproject.models.dto.JwtRequest;
import kz.romanb.onelabproject.models.dto.JwtResponse;
import kz.romanb.onelabproject.models.entities.AccessToken;
import kz.romanb.onelabproject.models.entities.RefreshToken;
import kz.romanb.onelabproject.models.entities.Role;
import kz.romanb.onelabproject.models.entities.User;
import kz.romanb.onelabproject.security.JwtProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    @Mock
    UserService userService;
    @Mock
    AccessTokenService accessTokenService;
    @Mock
    RefreshTokenService refreshTokenService;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtProvider jwtProvider;
    @InjectMocks
    AuthService authService;

    String accessTokenStr;
    String refreshTokenStr;
    JwtRequest jwtRequest;
    User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        accessTokenStr = "Access";
        refreshTokenStr = "Refresh";
        jwtRequest = new JwtRequest();
        jwtRequest.setEmail("user@mail.ru");
        jwtRequest.setPassword("123");
        user = User.builder().id(1L).email(jwtRequest.getEmail()).password(jwtRequest.getPassword()).build();
    }

    @Test
    void testLoginWithUpdateToken() {
        when(userService.loadUserByUsername(jwtRequest.getEmail())).thenReturn(user);
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtProvider.generateAccessToken(any())).thenReturn(accessTokenStr);
        when(jwtProvider.generateRefreshToken(any())).thenReturn(refreshTokenStr);
        when(jwtProvider.getAccessClaims(accessTokenStr)).thenReturn(mockedAccessTokenClaims());
        when(jwtProvider.getRefreshClaims(refreshTokenStr)).thenReturn(mockedRefreshTokenClaims());
        when(accessTokenService.findTokenByUser(user)).thenReturn(Optional.of(AccessToken.builder().build()));
        doNothing().when(accessTokenService).updateToken(any());
        when(refreshTokenService.findTokenByUser(user)).thenReturn(Optional.of(RefreshToken.builder().build()));
        doNothing().when(refreshTokenService).updateToken(any());

        JwtResponse jwtResponse = authService.login(jwtRequest);

        assertEquals(accessTokenStr, jwtResponse.getAccessToken());
        assertEquals(refreshTokenStr, jwtResponse.getRefreshToken());
        verify(userService, times(1)).loadUserByUsername(jwtRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(any(), any());
        verify(jwtProvider, times(2)).getAccessClaims(any());
        verify(jwtProvider, times(2)).getRefreshClaims(any());
        verify(accessTokenService, times(1)).findTokenByUser(user);
        verify(accessTokenService, times(1)).updateToken(any());
        verify(refreshTokenService, times(1)).findTokenByUser(user);
        verify(refreshTokenService, times(1)).updateToken(any());
    }

    @Test
    void testLoginWithSaveTokens() {
        when(userService.loadUserByUsername(jwtRequest.getEmail())).thenReturn(user);
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtProvider.generateAccessToken(any())).thenReturn(accessTokenStr);
        when(jwtProvider.generateRefreshToken(any())).thenReturn(refreshTokenStr);
        when(jwtProvider.getAccessClaims(accessTokenStr)).thenReturn(mockedAccessTokenClaims());
        when(jwtProvider.getRefreshClaims(refreshTokenStr)).thenReturn(mockedRefreshTokenClaims());
        when(accessTokenService.findTokenByUser(user)).thenReturn(Optional.empty());
        doNothing().when(accessTokenService).saveToken(any());
        when(refreshTokenService.findTokenByUser(user)).thenReturn(Optional.empty());
        doNothing().when(refreshTokenService).saveToken(any());

        JwtResponse jwtResponse = authService.login(jwtRequest);

        assertEquals(accessTokenStr, jwtResponse.getAccessToken());
        assertEquals(refreshTokenStr, jwtResponse.getRefreshToken());
        verify(userService, times(1)).loadUserByUsername(jwtRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(any(), any());
        verify(jwtProvider, times(2)).getAccessClaims(any());
        verify(jwtProvider, times(2)).getRefreshClaims(any());
        verify(accessTokenService, times(1)).findTokenByUser(user);
        verify(accessTokenService, times(1)).saveToken(any());
        verify(refreshTokenService, times(1)).findTokenByUser(user);
        verify(refreshTokenService, times(1)).saveToken(any());
    }

    @Test
    void testLoginWhenUserDoesNotExists(){
        when(userService.loadUserByUsername(jwtRequest.getEmail())).thenThrow(UsernameNotFoundException.class);

        assertThrows(UsernameNotFoundException.class, () -> authService.login(jwtRequest));

        verify(userService, times(1)).loadUserByUsername(jwtRequest.getEmail());
        verify(passwordEncoder, times(0)).matches(any(), any());
        verify(jwtProvider, times(0)).getAccessClaims(any());
        verify(jwtProvider, times(0)).getRefreshClaims(any());
        verify(accessTokenService, times(0)).findTokenByUser(user);
        verify(accessTokenService, times(0)).updateToken(any());
        verify(accessTokenService, times(0)).saveToken(any());
        verify(refreshTokenService, times(0)).findTokenByUser(user);
        verify(refreshTokenService, times(0)).updateToken(any());
        verify(refreshTokenService, times(0)).saveToken(any());
    }

    @Test
    void testLoginWithBadCredentials(){
        when(userService.loadUserByUsername(jwtRequest.getEmail())).thenReturn(user);
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThrows(AuthException.class, () -> authService.login(jwtRequest));

        verify(userService, times(1)).loadUserByUsername(jwtRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(any(), any());
        verify(jwtProvider, times(0)).getAccessClaims(any());
        verify(jwtProvider, times(0)).getRefreshClaims(any());
        verify(accessTokenService, times(0)).findTokenByUser(user);
        verify(accessTokenService, times(0)).updateToken(any());
        verify(accessTokenService, times(0)).saveToken(any());
        verify(refreshTokenService, times(0)).findTokenByUser(user);
        verify(refreshTokenService, times(0)).updateToken(any());
        verify(refreshTokenService, times(0)).saveToken(any());
    }

    @Test
    void testGenerateNewAccessTokenWithValidRefreshToken(){
        when(jwtProvider.validateRefreshToken(refreshTokenStr)).thenReturn(true);
        when(jwtProvider.getRefreshClaims(refreshTokenStr)).thenReturn(mockedRefreshTokenClaims());
        when(userService.loadUserByUsername(user.getEmail())).thenReturn(user);
        when(refreshTokenService.findTokenByUser(user)).thenReturn(Optional.of(RefreshToken.builder().refreshToken(refreshTokenStr).build()));
        when(jwtProvider.generateAccessToken(user)).thenReturn(accessTokenStr);
        when(jwtProvider.getAccessClaims(accessTokenStr)).thenReturn(mockedAccessTokenClaims());
        doNothing().when(accessTokenService).updateToken(any());

        JwtResponse jwtResponse = authService.getNewAccessToken(refreshTokenStr);

        assertEquals(accessTokenStr, jwtResponse.getAccessToken());
        assertNull(jwtResponse.getRefreshToken());
        verify(jwtProvider, times(1)).validateRefreshToken(refreshTokenStr);
        verify(jwtProvider, times(1)).getRefreshClaims(refreshTokenStr);
        verify(userService, times(1)).loadUserByUsername(user.getEmail());
        verify(refreshTokenService, times(1)).findTokenByUser(user);
        verify(jwtProvider, times(1)).generateAccessToken(user);
        verify(jwtProvider, times(2)).getAccessClaims(accessTokenStr);
        verify(accessTokenService, times(1)).updateToken(any());
    }

    @Test
    void testGenerateNewAccessTokenWithNonValidRefreshToken(){
        when(jwtProvider.validateRefreshToken(refreshTokenStr)).thenReturn(false);

        assertThrows(AuthException.class, () -> authService.getNewAccessToken(refreshTokenStr));
    }

    @Test
    void testGenerateNewRefreshTokenWithValidRefreshToken(){
        when(jwtProvider.validateRefreshToken(refreshTokenStr)).thenReturn(true);
        when(jwtProvider.getRefreshClaims(refreshTokenStr)).thenReturn(mockedRefreshTokenClaims());
        when(userService.loadUserByUsername(user.getEmail())).thenReturn(user);
        when(refreshTokenService.findTokenByUser(user)).thenReturn(Optional.of(RefreshToken.builder().refreshToken(refreshTokenStr).build()));
        when(jwtProvider.generateAccessToken(user)).thenReturn(accessTokenStr);
        when(jwtProvider.getAccessClaims(accessTokenStr)).thenReturn(mockedAccessTokenClaims());
        when(jwtProvider.generateRefreshToken(user)).thenReturn(refreshTokenStr);
        when(jwtProvider.getRefreshClaims(refreshTokenStr)).thenReturn(mockedRefreshTokenClaims());
        doNothing().when(accessTokenService).updateToken(any());
        doNothing().when(refreshTokenService).updateToken(any());

        JwtResponse jwtResponse = authService.getNewRefreshToken(refreshTokenStr);

        assertEquals(accessTokenStr, jwtResponse.getAccessToken());
        assertEquals(refreshTokenStr, jwtResponse.getRefreshToken());
        verify(jwtProvider, times(1)).validateRefreshToken(refreshTokenStr);
        verify(jwtProvider, times(3)).getRefreshClaims(refreshTokenStr);
        verify(userService, times(1)).loadUserByUsername(user.getEmail());
        verify(refreshTokenService, times(1)).findTokenByUser(user);
        verify(jwtProvider, times(1)).generateAccessToken(user);
        verify(jwtProvider, times(2)).getAccessClaims(accessTokenStr);
        verify(jwtProvider, times(1)).generateRefreshToken(user);
        verify(accessTokenService, times(1)).updateToken(any());
        verify(refreshTokenService, times(1)).updateToken(any());
    }

    @Test
    void testGenerateNewRefreshTokenWithNonValidRefreshToken(){
        when(jwtProvider.validateRefreshToken(refreshTokenStr)).thenReturn(false);

        assertThrows(AuthException.class, () -> authService.getNewRefreshToken(refreshTokenStr));
    }

    @Test
    void logoutWithValidToken(){
        when(jwtProvider.validateRefreshToken(refreshTokenStr)).thenReturn(true);
        when(jwtProvider.getRefreshClaims(refreshTokenStr)).thenReturn(mockedRefreshTokenClaims());
        when(userService.loadUserByUsername(user.getEmail())).thenReturn(user);
        when(accessTokenService.deleteToken(user)).thenReturn(true);
        when(refreshTokenService.deleteToken(user)).thenReturn(true);

        boolean result = authService.logout(refreshTokenStr);

        assertTrue(result);
        verify(jwtProvider, times(1)).validateRefreshToken(refreshTokenStr);
        verify(jwtProvider, times(1)).getRefreshClaims(refreshTokenStr);
        verify(userService, times(1)).loadUserByUsername(user.getEmail());
        verify(accessTokenService, times(1)).deleteToken(user);
        verify(refreshTokenService, times(1)).deleteToken(user);
    }

    @Test
    void testLogoutWithNonValidToken(){
        when(jwtProvider.validateRefreshToken(refreshTokenStr)).thenReturn(false);

        boolean result = authService.logout(refreshTokenStr);

        assertFalse(result);
        verify(jwtProvider, times(1)).validateRefreshToken(refreshTokenStr);
        verify(jwtProvider, never()).getRefreshClaims(refreshTokenStr);
        verify(userService, never()).loadUserByUsername(user.getEmail());
        verify(accessTokenService, never()).deleteToken(user);
        verify(refreshTokenService, never()).deleteToken(user);
    }

    private Claims mockedAccessTokenClaims() {
        Claims claims = new DefaultClaims();
        claims.setSubject(user.getEmail());
        claims.setIssuedAt(new Date());
        claims.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60));
        return claims;
    }

    private Claims mockedRefreshTokenClaims() {
        Claims claims = new DefaultClaims();
        claims.setSubject(user.getEmail());
        claims.setIssuedAt(new Date());
        claims.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7));
        return claims;
    }
}