package queueless.service;

import org.springframework.stereotype.Service;
import queueless.exception.QueuelessException;
import queueless.model.Business;
import queueless.model.Queue;
import queueless.model.QueueEntry;
import queueless.model.User;
import queueless.model.EntryStatus;
import queueless.repository.*;
import java.util.*;

@Service
public class BusinessService extends BaseService {

    private final BusinessRepository businessRepository;
    private final QueueRepository queueRepository;
    private final QueueEntryRepository queueEntryRepository;

    public BusinessService(BusinessRepository businessRepository, QueueRepository queueRepository, QueueEntryRepository queueEntryRepository) {
        this.businessRepository = businessRepository;
        this.queueRepository = queueRepository;
        this.queueEntryRepository = queueEntryRepository;
    }

    @Override
    public String getServiceName() { return "BusinessService"; }

    public Optional<Business> getProfile(User owner) {
        List<Business> existing = businessRepository.findByOwner(owner);
        return existing.isEmpty() ? Optional.empty() : Optional.of(existing.get(0));
    }

    public Business getOrCreateProfile(User owner, String name, String location, String serviceType) throws QueuelessException {
        Optional<Business> profile = getProfile(owner);
        if (profile.isPresent()) {
            Business b = profile.get();
            if (name != null) b.setName(name);
            if (location != null) b.setLocation(location);
            if (serviceType != null) b.setServiceType(serviceType);
            return businessRepository.save(b);
        }
        
        if (name == null || location == null) {
            throw new QueuelessException("Name and location are required for new profile");
        }
        
        return businessRepository.save(new Business(owner, name, location, serviceType));
    }

    public Queue createQueue(Business business, String serviceName, int avgTime) {
        return queueRepository.save(new Queue(business, serviceName, avgTime));
    }

    public List<Map<String, Object>> listQueuesWithEntries(Business business) {
        List<Queue> queues = queueRepository.findByBusiness(business);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Queue q : queues) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", q.getId());
            map.put("service_name", q.getServiceName());
            map.put("avg_service_time", q.getAvgServiceTime());
            
            List<QueueEntry> waiting = queueEntryRepository.findByQueueAndStatusOrderByJoinTimeAsc(q, EntryStatus.WAITING);
            map.put("waiting_count", waiting.size());
            
            if (!waiting.isEmpty()) {
                QueueEntry next = waiting.get(0);
                Map<String, Object> nextMap = new HashMap<>();
                nextMap.put("id", next.getId());
                nextMap.put("position", next.getPosition());
                nextMap.put("name", next.getUser().getName());
                nextMap.put("join_time", next.getJoinTime());
                map.put("next_customer", nextMap);
            } else {
                map.put("next_customer", null);
            }
            
            result.add(map);
        }
        return result;
    }

    public boolean callNext(int queueId) {
        Queue queue = queueRepository.findById(queueId).orElseThrow();
        List<QueueEntry> waiting = queueEntryRepository.findByQueueAndStatusOrderByJoinTimeAsc(queue, EntryStatus.WAITING);
        if (waiting.isEmpty()) return false;
        
        QueueEntry next = waiting.get(0);
        next.setStatus(EntryStatus.SERVED);
        queueEntryRepository.save(next);
        return true;
    }
}
