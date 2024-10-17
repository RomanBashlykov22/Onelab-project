package kz.romanb.onelabproject.repositories;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
    T save(T t);

    Optional<T> findById(ID id);

    List<T> findAll();
}
