package queueless.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import queueless.model.Queue;
import queueless.model.Business;
import java.util.List;

public interface QueueRepository extends JpaRepository<Queue, Integer> {
    List<Queue> findByBusiness(Business business);
    List<Queue> findByActiveTrue();
    long countByActiveTrue();
}
