package queueless.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import queueless.model.User;
import queueless.model.Role;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    long countByRole(Role role);
}
