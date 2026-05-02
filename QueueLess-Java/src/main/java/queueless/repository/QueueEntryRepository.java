package queueless.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import queueless.model.QueueEntry;
import queueless.model.Queue;
import queueless.model.User;
import java.util.List;
import java.util.Optional;

public interface QueueEntryRepository extends JpaRepository<QueueEntry, Integer> {
    List<QueueEntry> findByQueue(Queue queue);
    List<QueueEntry> findByUser(User user);
    List<QueueEntry> findByQueueAndStatusOrderByJoinTimeAsc(Queue queue, queueless.model.EntryStatus status);
    Optional<QueueEntry> findByQueueAndUserAndStatus(Queue queue, User user, queueless.model.EntryStatus status);
    long countByStatus(queueless.model.EntryStatus status);
}
