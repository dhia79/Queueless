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
        int position = (int) queueEntryRepository.findByQueue(q).stream().filter(e -> e.getStatus() == EntryStatus.WAITING).count() + 1;
        QueueEntry entry = new QueueEntry(user, q, position);
        return queueEntryRepository.save(entry);
    }

    public java.util.Map<String, Object> getQueueStatus(User user, Integer queueId) throws QueuelessException {
        System.out.println("[Debug] getQueueStatus for user: " + user.getEmail() + " queue: " + queueId);
        Queue q = queueRepository.findById(queueId).orElseThrow(() -> new QueuelessException("Queue non trouvée"));
        QueueEntry userEntry = queueEntryRepository.findByQueueAndUserAndStatus(q, user, EntryStatus.WAITING)
                .or(() -> queueEntryRepository.findByQueueAndUserAndStatus(q, user, EntryStatus.SERVED))
                .orElseThrow(() -> new QueuelessException("Aucune entrée active dans cette file"));
        System.out.println("[Debug] Found userEntry: " + userEntry.getId() + " status: " + userEntry.getStatus());

        List<QueueEntry> waitingEntries = queueEntryRepository.findByQueueAndStatusOrderByJoinTimeAsc(q, EntryStatus.WAITING);
        
        int peopleAhead = 0;
        for (int i = 0; i < waitingEntries.size(); i++) {
            if (waitingEntries.get(i).getId() == userEntry.getId()) {
                peopleAhead = i;
                break;
            }
        }

        java.util.Map<String, Object> status = new java.util.HashMap<>();
        java.util.Map<String, Object> entryMap = new java.util.HashMap<>();
        entryMap.put("id", userEntry.getId());
        entryMap.put("position", peopleAhead + 1);
        entryMap.put("status", userEntry.getStatus().name().toLowerCase());
        entryMap.put("business_name", q.getBusiness().getName());
        entryMap.put("service_name", q.getServiceName());
        
        status.put("status", "success");
        status.put("entry", entryMap);
        status.put("people_ahead", peopleAhead);
        status.put("estimated_wait_time", peopleAhead * q.getAvgServiceTime());
        
        return status;
    }

    public void leaveQueue(User user, Integer queueId) throws QueuelessException {
        Queue q = queueRepository.findById(queueId).orElseThrow(() -> new QueuelessException("Queue non trouvée"));
        QueueEntry entry = queueEntryRepository.findByQueueAndUserAndStatus(q, user, EntryStatus.WAITING)
                .orElseThrow(() -> new QueuelessException("Pas d'entrée active dans cette file"));
        
        entry.setStatus(EntryStatus.CANCELLED);
        queueEntryRepository.save(entry);
    }
}
