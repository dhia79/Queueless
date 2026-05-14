package queueless.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import queueless.exception.QueuelessException;
import queueless.model.Role;
import queueless.model.User;
import queueless.service.AuthService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session) {
        try {
            String email = body.get("email");
            String password = body.get("password");
            if (email == null || password == null) throw new QueuelessException("Email and password required");
            
            User user = authService.login(email, password);
            session.setAttribute("user", user);
            return ResponseEntity.ok(Map.of("status", "success", "user", userMap(user)));
        } catch (QueuelessException e) {
            return ResponseEntity.status(400).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body, HttpSession session) {
        try {
            String roleStr = body.get("role") != null ? body.get("role").toLowerCase() : "customer";
            Role role;
            try {
                role = Role.valueOf(roleStr);
            } catch (Exception e) {
                role = Role.customer;
            }
            
            User newUser = authService.register(body.get("name"), body.get("email"), body.get("password"), role);
            session.setAttribute("user", newUser);
            return ResponseEntity.ok(Map.of("status", "success", "user", userMap(newUser)));
        } catch (QueuelessException e) {
            return ResponseEntity.status(400).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @GetMapping("/check")
    public ResponseEntity<?> check(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return ResponseEntity.ok(Map.of("status", "success", "user", userMap(user)));
        }
        return ResponseEntity.status(401).build();
    }

    private Map<String, Object> userMap(User u) {
        if (u == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("id", u.getId());
        map.put("name", u.getName());
        map.put("email", u.getEmail());
        map.put("role", u.getRole().name().toLowerCase());
        return map;
    }
}
