package queueless.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import queueless.exception.QueuelessException;
import queueless.model.*;
import queueless.service.*;
import java.util.*;

/**
 * Contrôleur de compatibilité pour gérer les anciens appels PHP du frontend.
 */
@RestController
@RequestMapping("/api")
public class LegacyApiController {

    private final AuthService authService;
    private final UserQueueService userQueueService;
    private final BusinessService businessService;
    private final AdminService adminService;

    public LegacyApiController(AuthService authService, UserQueueService userQueueService, 
                               BusinessService businessService, AdminService adminService) {
        this.authService = authService;
        this.userQueueService = userQueueService;
        this.businessService = businessService;
        this.adminService = adminService;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        System.out.println("LegacyApiController PostConstruct: authService = " + authService);
    }


    // ── auth.php ──────────────────────────────────────────────────────────
    @RequestMapping(value = "/auth.php", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> handleAuth(@RequestParam(name = "action") String action, @RequestBody(required = false) Map<String, String> body, HttpSession session) {
        System.out.println("Processing Auth Action: " + action + " | Body: " + body);
        try {
            if (action.equals("login") || action.equals("register")) {
                if (body == null) return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Requête vide"));
            }

            switch (action) {
                case "login":
                    String email = body.get("email");
                    String pass = body.get("password");
                    if (email == null || pass == null) throw new QueuelessException("Email and password required");
                    User user = authService.login(email, pass);
                    session.setAttribute("user", user);
                    return ResponseEntity.ok(Map.of("status", "success", "user", userMap(user)));
                case "register":
                    User newUser = authService.register(body.get("name"), body.get("email"), body.get("password"), Role.valueOf(body.get("role").toUpperCase()));
                    session.setAttribute("user", newUser);
                    return ResponseEntity.ok(Map.of("status", "success", "user", userMap(newUser)));
                case "logout":
                    session.invalidate();
                    return ResponseEntity.ok(Map.of("status", "success"));
                case "check":
                    User curr = (User) session.getAttribute("user");
                    return curr != null ? ResponseEntity.ok(Map.of("status", "success", "user", userMap(curr))) : ResponseEntity.status(401).build();
                default:
                    return ResponseEntity.badRequest().body("Action inconnue");
            }
        } catch (QueuelessException e) {
            return ResponseEntity.status(400).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    private Map<String, Object> userMap(User u) {
        if (u == null) return null;
        return Map.of(
            "id", u.getId(),
            "name", u.getName(),
            "email", u.getEmail(),
            "role", u.getRole().name().toLowerCase()
        );
    }


    // ── user_queue.php ────────────────────────────────────────────────────
    @RequestMapping(value = "/user_queue.php", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> handleUserQueue(@RequestParam(name = "action") String action, @RequestParam(name = "queueId", required = false) Integer queueId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).build();

        try {
            switch (action) {
                case "list_businesses":
                    return ResponseEntity.ok(userQueueService.listBusinesses());
                case "list_queues":
                    return ResponseEntity.ok(userQueueService.listActiveQueues());
                case "join":
                    return ResponseEntity.ok(userQueueService.joinQueue(user, queueId));
                default:
                    return ResponseEntity.badRequest().body("Action inconnue");
            }
        } catch (QueuelessException e) {
            return ResponseEntity.status(400).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    // ── business_queue.php ────────────────────────────────────────────────
    @RequestMapping(value = "/business_queue.php", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> handleBusinessQueue(@RequestParam(name = "action") String action, @RequestBody(required = false) Map<String, Object> body, @RequestParam(name = "queueId", required = false) Integer queueId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.BUSINESS) return ResponseEntity.status(403).build();

        try {
            switch (action) {
                case "profile":
                case "get_profile":
                    return ResponseEntity.ok(businessService.getOrCreateProfile(user, null, null, null));
                case "create_profile":
                    return ResponseEntity.ok(businessService.getOrCreateProfile(user, (String)body.get("name"), (String)body.get("location"), (String)body.get("service_type")));
                case "create_queue":
                    Business biz = businessService.getOrCreateProfile(user, null, null, null);
                    return ResponseEntity.ok(businessService.createQueue(biz, (String)body.get("service_name"), Integer.parseInt(body.get("avg_service_time").toString())));
                case "list_queues":
                    Business b = businessService.getOrCreateProfile(user, null, null, null);
                    return ResponseEntity.ok(businessService.listQueuesWithEntries(b));
                case "call_next":
                    return ResponseEntity.ok(Map.of("success", businessService.callNext(queueId)));
                default:
                    return ResponseEntity.badRequest().body("Action inconnue");
            }
        } catch (QueuelessException e) {
            return ResponseEntity.status(400).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    // ── admin.php ─────────────────────────────────────────────────────────
    @RequestMapping(value = "/admin.php", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> handleAdmin(@RequestParam(name = "action") String action, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.ADMIN) return ResponseEntity.status(403).build();

        switch (action) {
            case "stats":
                return ResponseEntity.ok(adminService.getStats());
            case "list_users":
                return ResponseEntity.ok(adminService.listAllUsers());
            default:
                return ResponseEntity.badRequest().body("Action inconnue");
        }
    }
}
