package kz.romanb.onelabproject.repositories;

import kz.romanb.onelabproject.models.entities.RefreshToken;
import kz.romanb.onelabproject.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUser(User user);

    void deleteByUser(User user);
}
