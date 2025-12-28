package tr.edu.kalyon.nur.university.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.edu.kalyon.nur.university.controller.dto.AuthResponse;
import tr.edu.kalyon.nur.university.controller.dto.LoginRequest;
import tr.edu.kalyon.nur.university.controller.dto.RegisterRequest;
import tr.edu.kalyon.nur.university.model.User;
import tr.edu.kalyon.nur.university.service.JwtService;
import tr.edu.kalyon.nur.university.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        User user = userService.login(req.getUsername(), req.getPassword());
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
        User user = userService.register(req);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
