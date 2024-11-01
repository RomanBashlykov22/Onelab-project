package kz.romanb.onelabproject.controllers;

import jakarta.validation.Valid;
import kz.romanb.onelabproject.exceptions.DBRecordNotFoundException;
import kz.romanb.onelabproject.models.dto.RegistrationRequest;
import kz.romanb.onelabproject.models.dto.UserDto;
import kz.romanb.onelabproject.models.entities.User;
import kz.romanb.onelabproject.security.JwtAuthentication;
import kz.romanb.onelabproject.services.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/registration")
    public UserDto registration(@Valid @RequestBody RegistrationRequest request) {
        return modelMapper.map(userService.registration(request), UserDto.class);
    }

    @GetMapping("/users/getAllUsers")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserDto> getAllUsers(){
        return userService.findAllUsers().stream().map(e -> modelMapper.map(e, UserDto.class)).toList();
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public UserDto getUserById(@PathVariable Long userId){
        Optional<User> userOptional = userService.findUserById(userId);
        if(userOptional.isEmpty()){
            throw new DBRecordNotFoundException("Пользователь с id " + userId + " не существует");
        }
        return modelMapper.map(userOptional.get(), UserDto.class);
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String deleteUser(@PathVariable Long userId){
        return userService.deleteUser(userId);
    }
}
