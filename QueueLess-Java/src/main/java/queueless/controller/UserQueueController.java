package queueless.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import queueless.exception.QueuelessException;
import queueless.model.User;
import queueless.service.UserQueueService;

import java.util.Map;

@RestController
@RequestMapping("/api/user-queue")
public class UserQueueController {

    private final UserQueueService userQueueService;

    public UserQueueController(UserQueueService userQueueService) {
        this.userQueueService = userQueueService;
    }

    @GetMapping("/businesses")
    public ResponseEntity<?> listBusinesses(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(Map.of("status", "success", "businesses", userQueueService.listBusinesses()));
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinQueue(@RequestBody Map<String, Object> body, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).build();

        try {
            Integer queueId = Integer.parseInt(body.get("queue_id").toString());
            return ResponseEntity.ok(Map.of("status", "success", "entry", userQueueService.joinQueue(user, queueId)));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus(@RequestParam Integer queue_id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).build();

        try {
            return ResponseEntity.ok(userQueueService.getQueueStatus(user, queue_id));
        } catch (QueuelessException e) {
            return ResponseEntity.status(400).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @PostMapping("/leave")
    public ResponseEntity<?> leaveQueue(@RequestBody Map<String, Object> body, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).build();

        try {
            Integer queueId = Integer.parseInt(body.get("queue_id").toString());
            userQueueService.leaveQueue(user, queueId);
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
