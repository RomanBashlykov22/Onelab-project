package kz.romanb.onelabproject.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.romanb.onelabproject.models.dto.*;
import kz.romanb.onelabproject.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication controller",
        description = "Контроллер для входа и выхода из приложения, генерации JWT-токенов"
)
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
            summary = "Вход в приложение",
            description = "Позволяет получить доступ к приложению"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный вход в приложение",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "403", description = "Не удалось зайти в приложение",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody @Parameter(description = "Содержит данные пользователя: логин (email) и пароль") JwtRequest jwtRequest) {
        return ResponseEntity.ok(authService.login(jwtRequest));
    }

    @PostMapping("/new-access-token")
    @Operation(
            summary = "Обновление токена",
            description = "Позволяет получить новый Access-токен"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Токен обновлен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "403", description = "Не удалось обновить токен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    public ResponseEntity<JwtResponse> getNewAccessToken(@RequestBody @Parameter(description = "Действующий Refresh-токена пользователя") JwtRefreshRequest request) {
        return ResponseEntity.ok(authService.getNewAccessToken(request.refreshToken()));
    }

    @PostMapping("/new-refresh-token")
    @Operation(
            summary = "Обновление токена",
            description = "Позволяет получить новый Refresh-токен"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Токен обновлен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "403", description = "Не удалось обновить токен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)))
    })
    public ResponseEntity<JwtResponse> getNewRefreshToken(@RequestBody @Parameter(description = "Действующий Refresh-токена пользователя") JwtRefreshRequest request) {
        return ResponseEntity.ok(authService.getNewRefreshToken(request.refreshToken()));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Выход из приложения",
            description = "Позволяет из приложения"
    )
    public ResponseEntity<HttpStatus> logout(@RequestBody @Parameter(description = "Действующий Refresh-токена пользователя") JwtRefreshRequest request) {
        return authService.logout(request.refreshToken()) ? ResponseEntity.ok(HttpStatus.OK) : ResponseEntity.badRequest().body(HttpStatus.BAD_REQUEST);
    }
}
