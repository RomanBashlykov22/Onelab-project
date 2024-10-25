package kz.romanb.onelabproject.repositories;

import kz.romanb.onelabproject.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
