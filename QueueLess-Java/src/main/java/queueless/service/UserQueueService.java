package queueless.service;

import org.springframework.stereotype.Service;
import queueless.exception.QueuelessException;
import queueless.model.*;
import queueless.repository.*;
import java.util.List;

@Service
public class UserQueueService extends BaseService {

    private final BusinessRepository businessRepository;
    private final QueueRepository queueRepository;
    private final QueueEntryRepository queueEntryRepository;

    public UserQueueService(BusinessRepository businessRepository, QueueRepository queueRepository, QueueEntryRepository queueEntryRepository) {
        this.businessRepository = businessRepository;
        this.queueRepository = queueRepository;
        this.queueEntryRepository = queueEntryRepository;
    }

    @Override
    public String getServiceName() { return "UserQueueService"; }

    public List<Business> listBusinesses() {
        return businessRepository.findAll();
    }

    public List<Queue> listActiveQueues() {
        return queueRepository.findAll();
    }

    public QueueEntry joinQueue(User user, Integer queueId) throws QueuelessException {
        Queue q = queueRepository.findById(queueId).orElseThrow(() -> new QueuelessException("Queue non trouvée"));
        int position = queueEntryRepository.findByQueue(q).size() + 1;
        QueueEntry entry = new QueueEntry(user, q, position);
        return queueEntryRepository.save(entry);
    }
}
