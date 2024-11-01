package kz.romanb.onelabproject.services;

import jakarta.persistence.PersistenceException;
import kz.romanb.onelabproject.models.entities.RefreshToken;
import kz.romanb.onelabproject.models.entities.User;
import kz.romanb.onelabproject.repositories.RefreshTokenRepository;
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
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findTokenByUser(User user) {
        return refreshTokenRepository.findByUser(user);
    }

    public void updateToken(RefreshToken refreshToken) {
        RefreshToken oldRefreshToken = findTokenByUser(refreshToken.getUser()).orElse(null);
        if (oldRefreshToken == null) {
            log.error("Access token для пользователя {} не найден", refreshToken.getUser().getEmail());
            throw new UsernameNotFoundException("Access token для пользователя " + refreshToken.getUser().getEmail() + " не найден");
        }
        refreshToken.setId(oldRefreshToken.getId());
        saveToken(refreshToken);
    }

    public void saveToken(RefreshToken refreshToken) {
        try {
            refreshTokenRepository.save(refreshToken);
        } catch (Exception e) {
            log.error("Access token для пользователя {} не создан", refreshToken.getUser().getEmail());
            throw new PersistenceException("Access token не создан", e);
        }
    }

    public boolean deleteToken(User user) {
        try {
            refreshTokenRepository.deleteByUser(user);
            log.info("Refresh токен пользователя {} удален", user.getEmail());
            return true;
        } catch (Exception e) {
            log.error("Refresh токен пользователя {} не удален", user.getEmail());
            return false;
        }
    }
}

