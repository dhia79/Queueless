package queueless.service;

import org.springframework.stereotype.Service;
import queueless.repository.UserRepository;
import queueless.model.User;
import java.util.List;

@Service
public class AdminService extends BaseService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String getServiceName() { return "AdminService"; }

    public List<User> listAllUsers() {
        return userRepository.findAll();
    }

    public long getUserCount() {
        return userRepository.count();
    }

    public java.util.Map<String, Object> getStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalUsers", userRepository.count());
        return stats;
    }
}
