package queueless.service;

import org.springframework.stereotype.Service;
import queueless.repository.UserRepository;
import queueless.model.User;
import java.util.List;

@Service
public class AdminService extends BaseService {

    private final UserRepository userRepository;
    private final queueless.repository.BusinessRepository businessRepository;
    private final queueless.repository.QueueRepository queueRepository;

    public AdminService(UserRepository userRepository, queueless.repository.BusinessRepository businessRepository, queueless.repository.QueueRepository queueRepository) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.queueRepository = queueRepository;
    }

    @Override
    public String getServiceName() { return "AdminService"; }

    public List<User> listAllUsers() {
        return userRepository.findAll();
    }

    public List<java.util.Map<String, Object>> listAllBusinesses() {
        return businessRepository.findAll().stream().map(b -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("business_id", b.getId());
            map.put("business_name", b.getName());
            map.put("user_id", b.getOwner() != null ? b.getOwner().getId() : null);
            map.put("owner_name", b.getOwner() != null ? b.getOwner().getName() : "Unknown");
            map.put("email", b.getOwner() != null ? b.getOwner().getEmail() : "N/A");
            map.put("location", b.getLocation());
            map.put("service_type", b.getServiceType());
            map.put("created_at", b.getCreatedAt());
            return map;
        }).toList();
    }

    public List<java.util.Map<String, Object>> listAllCustomers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == queueless.model.Role.customer)
                .map(u -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", u.getId());
                    map.put("name", u.getName());
                    map.put("email", u.getEmail());
                    map.put("created_at", u.getCreatedAt());
                    return map;
                })
                .toList();
    }

    public void triggerManualSeed() {
        try {
            new queueless.util.DataSeeder(userRepository, businessRepository, queueRepository).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public java.util.Map<String, Object> getStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("total_users", userRepository.count());
        stats.put("total_businesses", businessRepository.count());
        stats.put("total_served", 0); // Mock for now
        return stats;
    }
}
