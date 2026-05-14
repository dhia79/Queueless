package queueless.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import queueless.exception.QueuelessException;
import queueless.model.Business;
import queueless.model.Role;
import queueless.model.User;
import queueless.service.BusinessService;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/business-queue")
public class BusinessQueueController {

    private final BusinessService businessService;

    public BusinessQueueController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.BUSINESS) return ResponseEntity.status(403).build();

        Optional<Business> bizProfile = businessService.getProfile(user);
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("status", "success");
        response.put("business", bizProfile.orElse(null));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.BUSINESS) return ResponseEntity.status(403).build();

        try {
            Business biz = businessService.getOrCreateProfile(user, body.get("name"), body.get("location"), body.get("service_type"));
            return ResponseEntity.ok(Map.of("status", "success", "business", biz));
        } catch (QueuelessException e) {
            return ResponseEntity.status(400).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping("/queues")
    public ResponseEntity<?> listQueues(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.BUSINESS) return ResponseEntity.status(403).build();

        try {
            Optional<Business> biz = businessService.getProfile(user);
            if (biz.isEmpty()) throw new QueuelessException("Profile non configuré");
            return ResponseEntity.ok(Map.of("status", "success", "queues", businessService.listQueuesWithEntries(biz.get())));
        } catch (QueuelessException e) {
            return ResponseEntity.status(400).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @PostMapping("/create-queue")
    public ResponseEntity<?> createQueue(@RequestBody Map<String, Object> body, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.BUSINESS) return ResponseEntity.status(403).build();

        try {
            Optional<Business> biz = businessService.getProfile(user);
            if (biz.isEmpty()) throw new QueuelessException("Profile non configuré");
            
            return ResponseEntity.ok(Map.of("status", "success", "queue", 
                businessService.createQueue(biz.get(), 
                    (String)body.get("service_name"), 
                    Integer.parseInt(body.get("avg_service_time").toString()))));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @PostMapping("/call-next")
    public ResponseEntity<?> callNext(@RequestBody Map<String, Object> body, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.BUSINESS) return ResponseEntity.status(403).build();

        try {
            Integer queueId = Integer.parseInt(body.get("queue_id").toString());
            boolean success = businessService.callNext(queueId);
            return ResponseEntity.ok(Map.of("status", success ? "success" : "error", "message", success ? "Appelé" : "File vide"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
