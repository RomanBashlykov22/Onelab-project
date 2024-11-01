package kz.romanb.onelabproject.services;

import jakarta.persistence.PersistenceException;
import kz.romanb.onelabproject.models.entities.AccessToken;
import kz.romanb.onelabproject.models.entities.User;
import kz.romanb.onelabproject.repositories.AccessTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccessTokenService {
    private final AccessTokenRepository accessTokenRepository;

    @Transactional(readOnly = true)
    public Optional<AccessToken> findTokenByUser(User user) {
        return accessTokenRepository.findByUser(user);
    }

    public void updateToken(AccessToken accessToken) {
        AccessToken oldAccessToken = findTokenByUser(accessToken.getUser()).orElse(null);
        if (oldAccessToken == null) {
            log.error("Access token для пользователя {} не найден", accessToken.getUser().getEmail());
            throw new UsernameNotFoundException("Access token для пользователя " + accessToken.getUser().getEmail() + " не найден");
        }
        accessToken.setId(oldAccessToken.getId());
        saveToken(accessToken);
    }

    public void saveToken(AccessToken accessToken) {
        try {
            accessTokenRepository.save(accessToken);
        } catch (Exception e) {
            log.error("Access token для пользователя {} не создан", accessToken.getUser().getEmail());
            throw new PersistenceException("Access token не создан", e);
        }
    }

    public boolean deleteToken(User user) {
        try {
            accessTokenRepository.deleteByUser(user);
            log.info("Access токен пользователя {} удален", user.getEmail());
            return true;
        } catch (Exception e) {
            log.error("Access токен пользователя {} не удален", user.getEmail());
            return false;
        }
    }
}
