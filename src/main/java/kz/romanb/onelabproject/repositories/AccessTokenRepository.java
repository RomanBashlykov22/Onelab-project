package kz.romanb.onelabproject.repositories;

import kz.romanb.onelabproject.models.entities.AccessToken;
import kz.romanb.onelabproject.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
    Optional<AccessToken> findByUser(User user);

    void deleteByUser(User user);
}