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

    public List<java.util.Map<String, Object>> listBusinesses() {
        return businessRepository.findAll().stream().map(b -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", b.getId());
            map.put("name", b.getName());
            map.put("location", b.getLocation());
            map.put("service_type", b.getServiceType());
            
            List<java.util.Map<String, Object>> queues = queueRepository.findByBusiness(b).stream().map(q -> {
                java.util.Map<String, Object> qMap = new java.util.HashMap<>();
                qMap.put("id", q.getId());
                qMap.put("service_name", q.getServiceName());
                qMap.put("avg_service_time", q.getAvgServiceTime());
                return qMap;
            }).toList();
            
            map.put("queues", queues);
            return map;
        }).toList();
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
