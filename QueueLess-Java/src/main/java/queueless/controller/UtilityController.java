package queueless.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import queueless.model.Role;
import queueless.model.User;
import queueless.service.AdminService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/utils")
public class UtilityController {

    private final AdminService adminService;

    public UtilityController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/seed")
    public ResponseEntity<?> triggerSeed(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null && user.getRole() != Role.ADMIN) return ResponseEntity.status(403).build();
        
        adminService.triggerManualSeed();
        return ResponseEntity.ok(Map.of("status", "success", "message", "Database seeded successfully"));
    }

    @GetMapping("/probe")
    public ResponseEntity<?> probe(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null && user.getRole() != Role.ADMIN) return ResponseEntity.status(403).build();

        Map<String, Object> diagnostics = new HashMap<>();
        diagnostics.put("java_version", System.getProperty("java.version"));
        diagnostics.put("os_name", System.getProperty("os.name"));
        diagnostics.put("server_time", new java.util.Date().toString());
        diagnostics.put("memory_free", Runtime.getRuntime().freeMemory() / (1024 * 1024) + " MB");
        diagnostics.put("memory_total", Runtime.getRuntime().totalMemory() / (1024 * 1024) + " MB");
        diagnostics.put("db_status", "Connected (JPA)");

        return ResponseEntity.ok(Map.of("status", "success", "diagnostics", diagnostics));
    }
}
