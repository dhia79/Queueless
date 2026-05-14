package queueless.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import queueless.model.Role;
import queueless.model.User;
import queueless.service.AdminService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.ADMIN) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(Map.of("status", "success", "stats", adminService.getStats()));
    }

    @GetMapping("/users")
    public ResponseEntity<?> listUsers(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.ADMIN) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(Map.of("status", "success", "users", adminService.listAllUsers()));
    }

    @GetMapping("/businesses")
    public ResponseEntity<?> listBusinesses(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.ADMIN) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(Map.of("status", "success", "businesses", adminService.listAllBusinesses()));
    }

    @GetMapping("/customers")
    public ResponseEntity<?> listCustomers(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.ADMIN) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(Map.of("status", "success", "customers", adminService.listAllCustomers()));
    }
}
