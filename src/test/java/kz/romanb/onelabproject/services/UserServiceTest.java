package kz.romanb.onelabproject.services;

import kz.romanb.onelabproject.entities.User;
import kz.romanb.onelabproject.exceptions.DBRecordNotFoundException;
import kz.romanb.onelabproject.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Mock
    UserRepository userRepository;
    @InjectMocks
    UserService userService;

    User user = null;
    User userFromDB = null;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder().build();
        userFromDB = User.builder().id(1L).name("Username").build();
    }

    @Test
    void testAddNewUserWithoutName() {
        when(userRepository.save(user)).thenThrow(new DataIntegrityViolationException("Name не может быть null"));
        assertThrows(DataIntegrityViolationException.class, () -> userService.addNewUser(user));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testAddNewUser() {
        user.setId(1L);
        user.setName("Username");
        when(userRepository.save(user)).thenReturn(userFromDB);
        User result = userService.addNewUser(user);
        assertNotNull(result);
        assertEquals(result.getId(), userFromDB.getId());
        assertEquals(result.getName(), userFromDB.getName());
        verify(userRepository, times(1)).save(user);
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
        assertEquals(result.get().getName(), userFromDB.getName());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testFindUserByIdWhenUserDoesNotExists() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenThrow(new DBRecordNotFoundException("Пользователь не существует"));
        assertThrows(DBRecordNotFoundException.class, () -> userService.findUserById(userId));
        verify(userRepository, times(1)).findById(userId);
    }
}