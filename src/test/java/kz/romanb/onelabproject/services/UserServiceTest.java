package kz.romanb.onelabproject.services;

import kz.romanb.onelabproject.exceptions.RegistrationException;
import kz.romanb.onelabproject.models.dto.RegistrationRequest;
import kz.romanb.onelabproject.models.entities.Role;
import kz.romanb.onelabproject.models.entities.User;
import kz.romanb.onelabproject.exceptions.DBRecordNotFoundException;
import kz.romanb.onelabproject.kafka.KafkaService;
import kz.romanb.onelabproject.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Mock
    UserRepository userRepository;
    @Mock
    KafkaService kafkaService;
    @Mock
    AccessTokenService accessTokenService;
    @Mock
    RefreshTokenService refreshTokenService;
    @Mock
    PasswordEncoder passwordEncoder;
    @InjectMocks
    UserService userService;

    RegistrationRequest request = null;
    User user = null;
    User userFromDB = null;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new RegistrationRequest("user@mail.ru", "123", "username");
        userFromDB = User.builder()
                .id(1L)
                .username("Username")
                .password("123")
                .email("user@mail.ru")
                .roles(Set.of(Role.USER))
                .isCredentialsNonExpired(true)
                .isEnabled(true)
                .isAccountNonLocked(true)
                .isAccountNonExpired(true)
                .build();
        user = User.builder()
                .username("Username")
                .password("123")
                .email("user@mail.ru")
                .roles(Set.of(Role.USER))
                .isCredentialsNonExpired(true)
                .isEnabled(true)
                .isAccountNonLocked(true)
                .isAccountNonExpired(true)
                .build();
    }

    @Test
    void testLoadUserByName() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(userFromDB));

        UserDetails result = userService.loadUserByUsername("user@mail.ru");

        assertNotNull(result);
        verify(userRepository, times(1)).findByEmail(any());
    }

    @Test
    void testLoadUserByUnknownName() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("user@mial.ru"));

        verify(userRepository, times(1)).findByEmail(any());
    }

    @Test
    void testRegistration() {
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(userFromDB);

        User result = userService.registration(request);

        assertNotNull(result);
        assertEquals(result.getId(), userFromDB.getId());
        assertEquals(result.getUsername(), userFromDB.getUsername());
        verify(userRepository, times(1)).findByEmail(request.email());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegistrationWithExistingEmail() {
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(userFromDB));

        assertThrows(RegistrationException.class, () -> userService.registration(request));
        verify(userRepository, times(1)).findByEmail(request.email());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testFindAllUsers() {
        List<User> users = new ArrayList<>(List.of(
                User.builder().build(),
                User.builder().build()
        ));
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.findAllUsers();

        assertNotNull(result);
        assertEquals(result.size(), users.size());
        assertFalse(result.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testFindUserByIdWhenUserExists() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(userFromDB));

        Optional<User> result = userService.findUserById(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        assertEquals(result.get().getUsername(), userFromDB.getUsername());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testFindUserByIdWhenUserDoesNotExists() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = userService.findUserById(userId);

        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void deleteUserWhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userFromDB));
        when(accessTokenService.deleteToken(userFromDB)).thenReturn(true);
        when(refreshTokenService.deleteToken(userFromDB)).thenReturn(true);
        doNothing().when(userRepository).delete(any());

        userService.deleteUser(1L);

        verify(userRepository, times(1)).findById(1L);
        verify(accessTokenService, times(1)).deleteToken(userFromDB);
        verify(refreshTokenService, times(1)).deleteToken(userFromDB);
        verify(userRepository, times(1)).delete(any());
    }

    @Test
    void deleteUserWhenUserDoesNotExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(DBRecordNotFoundException.class, () -> userService.deleteUser(1L));

        verify(userRepository, times(1)).findById(1L);
        verify(accessTokenService, never()).deleteToken(userFromDB);
        verify(refreshTokenService, never()).deleteToken(userFromDB);
        verify(userRepository, never()).delete(any());
    }
}