package queueless.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import queueless.model.Business;
import queueless.model.User;
import java.util.List;

public interface BusinessRepository extends JpaRepository<Business, Integer> {
    List<Business> findByOwner(User owner);
}
