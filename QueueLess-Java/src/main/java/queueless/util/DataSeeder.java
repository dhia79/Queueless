package queueless.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import queueless.model.*;
import queueless.repository.*;
import java.security.MessageDigest;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final QueueRepository queueRepository;

    public DataSeeder(UserRepository userRepository, BusinessRepository businessRepository, QueueRepository queueRepository) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.queueRepository = queueRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (businessRepository.count() > 0) {
            System.out.println("[DataSeeder] Data already exists, skipping...");
            return;
        }

        System.out.println("[DataSeeder] Seeding mock businesses...");

        // 1. Create a Primary Business Owner if not exists
        User bOwner = userRepository.findByEmail("business@test.com")
            .orElseGet(() -> {
                User newUser = new User("John Business", "business@test.com", hashPassword("business"), Role.business);
                return userRepository.save(newUser);
            });

        // 2. Create Businesses for this owner
        Business b1 = new Business(bOwner, "Global Services", "Paris, FR", "Consulting");
        Business b2 = new Business(bOwner, "Tech Solutions", "Lyon, FR", "Electronics");
        Business b3 = new Business(bOwner, "Health Center", "Marseille, FR", "Medical");
        
        businessRepository.save(b1);
        businessRepository.save(b2);
        businessRepository.save(b3);

        // 3. Create Queues for these businesses
        queueRepository.save(new Queue(b1, "General Support", 15));
        queueRepository.save(new Queue(b1, "Legal Consultation", 45));
        
        queueRepository.save(new Queue(b2, "Express Repair", 20));
        queueRepository.save(new Queue(b2, "Project Planning", 60));
        
        queueRepository.save(new Queue(b3, "General Practitioner", 25));
        queueRepository.save(new Queue(b3, "Pharmacy", 5));

        System.out.println("[DataSeeder] Seed complete!");
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return password;
        }
    }
}
