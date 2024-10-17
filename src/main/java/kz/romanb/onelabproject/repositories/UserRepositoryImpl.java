package kz.romanb.onelabproject.repositories;

import kz.romanb.onelabproject.entities.User;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class UserRepositoryImpl implements UserRepository {
    public static final List<User> users = new ArrayList<>();

    @Override
    public Optional<User> findById(Long id) {
        return users.stream().filter(u -> Objects.equals(u.getId(), id)).findFirst();
    }

    @Override
    public List<User> findAll() {
        return users;
    }

    @Override
    public User save(User user) {
        Optional<User> userOptional = findById(user.getId());
        userOptional.ifPresent(users::remove);
        users.add(user);
        sortUsers();
        return user;
    }

    private static void sortUsers() {
        users.sort(Comparator.comparing(User::getId));
    }
}
