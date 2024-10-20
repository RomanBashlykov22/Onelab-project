package kz.romanb.onelabproject.services;

import kz.romanb.onelabproject.entities.User;
import kz.romanb.onelabproject.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BankAccountService bankAccountService;
    private final CostCategoryService costCategoryService;

    public User addNewUser(User user) {
        return userRepository.save(user);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User findUserById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        User user = userOptional.get();
        bankAccountService.getAllUserAccounts(user);
        costCategoryService.getAllUserCostCategories(user);
        return user;
    }
}
