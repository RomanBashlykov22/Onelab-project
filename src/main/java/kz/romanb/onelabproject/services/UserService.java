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

    private final UserRepository userRepository;
    private final KafkaService kafkaService;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(() -> {
            log.error("Пользователь {} не найден", username);
            throw new UsernameNotFoundException("Пользователь с таким E-mail не найден");
        });
    }

    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
    public User registration(RegistrationRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.error("Попытка зарегистрировать пользователя с существующим E-mail");
            throw new RegistrationException("Пользователь с таким E-mail уже существует");
        }
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword().trim()))
                .roles(Collections.singleton(Role.USER))
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isEnabled(true)
                .isCredentialsNonExpired(true)
                .build();
        User saved = userRepository.save(user);
        kafkaService.sendMessage(saved.getId(), "Создание аккаунта");
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
        if(userOptional.isEmpty()) {
            log.error("Пользователь с id {} не найден", userId);
            throw new DBRecordNotFoundException("Пользователь не найден");
        }
        else{
            accessTokenService.deleteToken(userOptional.get());
            refreshTokenService.deleteToken(userOptional.get());
            userRepository.delete(userOptional.get());
            log.info("Пользователь {} успешно удален", userOptional.get().getEmail());
            return "Пользователь удален";
        }
    }
}
