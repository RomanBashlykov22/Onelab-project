package kz.romanb.onelabproject.services;

import kz.romanb.onelabproject.exceptions.RegistrationException;
import kz.romanb.onelabproject.models.dto.RegistrationRequest;
import kz.romanb.onelabproject.models.entities.Role;
import kz.romanb.onelabproject.models.entities.User;
import kz.romanb.onelabproject.exceptions.DBRecordNotFoundException;
import kz.romanb.onelabproject.kafka.KafkaService;
import kz.romanb.onelabproject.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService implements UserDetailsService {
    public static final String USER_WITH_EMAIL_DOES_NOT_EXISTS = "Пользователь с таким E-mail не найден";
    public static final String USER_WITH_EMAIL_ALREADY_EXISTS = "Пользователь с таким E-mail уже существует";
    public static final String USER_WITH_ID_DOES_NOT_EXISTS = "Пользователь с id %d не существует";

    private final UserRepository userRepository;
    private final KafkaService kafkaService;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(() -> {
            log.error("Пользователь {} не найден", username);
            throw new UsernameNotFoundException(USER_WITH_EMAIL_DOES_NOT_EXISTS);
        });
    }

    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
    public User registration(RegistrationRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            log.error("Попытка зарегистрировать пользователя с существующим E-mail");
            throw new RegistrationException(USER_WITH_EMAIL_ALREADY_EXISTS);
        }
        User user = User.builder()
                .email(request.email())
                .username(request.username())
                .password(passwordEncoder.encode(request.password().trim()))
                .roles(Collections.singleton(Role.USER))
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isEnabled(true)
                .isCredentialsNonExpired(true)
                .build();
        User saved = userRepository.save(user);
        kafkaService.sendMessage(saved.getEmail(), "Создание аккаунта");
        return saved;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.SUPPORTS)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.SUPPORTS)
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public String deleteUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            log.error("Пользователь с id {} не найден", userId);
            throw new DBRecordNotFoundException(String.format(USER_WITH_ID_DOES_NOT_EXISTS, userId));
        } else {
            accessTokenService.deleteToken(userOptional.get());
            refreshTokenService.deleteToken(userOptional.get());
            userRepository.delete(userOptional.get());
            log.info("Пользователь {} успешно удален", userOptional.get().getEmail());
            return "Пользователь удален";
        }
    }
}
