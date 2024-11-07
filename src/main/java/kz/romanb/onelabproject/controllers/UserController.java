package kz.romanb.onelabproject.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.romanb.onelabproject.exceptions.DBRecordNotFoundException;
import kz.romanb.onelabproject.models.dto.ErrorDto;
import kz.romanb.onelabproject.models.dto.RegistrationRequest;
import kz.romanb.onelabproject.models.dto.UserDto;
import kz.romanb.onelabproject.models.entities.User;
import kz.romanb.onelabproject.services.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "User controller", description = "Контроллер для управления пользователями")
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/registration")
    @Operation(summary = "Регистрация пользователя", description = "Позволяет зарегистрировать пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка при регистрации пользователя",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    public UserDto registration(@Valid @RequestBody @Parameter(description = "Содержит данные пользователя для регистрации: username, email и пароль") RegistrationRequest request) {
        return modelMapper.map(userService.registration(request), UserDto.class);
    }

    @GetMapping("/users/getAllUsers")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(
            summary = "Получения всех пользователей",
            description = "Позволяет получить список всех пользователей, зарегистрированных в приложении"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Получен список пользователей",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав для выполнения операции",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    @SecurityRequirement(name = "JWT")
    public List<UserDto> getAllUsers() {
        return userService.findAllUsers().stream().map(e -> modelMapper.map(e, UserDto.class)).toList();
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(
            summary = "Получение пользователя по ID",
            description = "Позволяет получить пользователя приложения по его ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Получен пользователь",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Пользователь не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав для выполнения операции",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    @SecurityRequirement(name = "JWT")
    public UserDto getUserById(@PathVariable @Parameter(description = "ID пользователя", example = "1") Long userId) {
        Optional<User> userOptional = userService.findUserById(userId);
        if (userOptional.isEmpty()) {
            throw new DBRecordNotFoundException(String.format(UserService.USER_WITH_ID_DOES_NOT_EXISTS, userId));
        }
        return modelMapper.map(userOptional.get(), UserDto.class);
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Удаление пользователя", description = "Позволяет удалить пользователя из приложения")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь удален"),
            @ApiResponse(responseCode = "400", description = "Пользователь не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав для выполнения операции",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    @SecurityRequirement(name = "JWT")
    public String deleteUser(@PathVariable @Parameter(description = "ID пользователя", example = "1") Long userId) {
        return userService.deleteUser(userId);
    }
}
