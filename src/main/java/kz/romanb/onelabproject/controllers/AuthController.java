package kz.romanb.onelabproject.controllers;

import jakarta.validation.Valid;
import kz.romanb.onelabproject.models.dto.JwtRefreshRequest;
import kz.romanb.onelabproject.models.dto.JwtRequest;
import kz.romanb.onelabproject.models.dto.JwtResponse;
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
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody JwtRequest jwtRequest) {
        return ResponseEntity.ok(authService.login(jwtRequest));
    }

    @PostMapping("/new-access-token")
    public ResponseEntity<JwtResponse> getNewAccessToken(@RequestBody JwtRefreshRequest request) {
        return ResponseEntity.ok(authService.getNewAccessToken(request.refreshToken()));
    }

    @PostMapping("/new-refresh-token")
    public ResponseEntity<JwtResponse> getNewRefreshToken(@RequestBody JwtRefreshRequest request) {
        return ResponseEntity.ok(authService.getNewRefreshToken(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<HttpStatus> logout(@RequestBody JwtRefreshRequest request) {
        return authService.logout(request.refreshToken()) ? ResponseEntity.ok(HttpStatus.OK) : ResponseEntity.badRequest().body(HttpStatus.BAD_REQUEST);
    }
}
