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

    public Business getOrCreateProfile(User owner, String name, String location, String serviceType) throws QueuelessException {
        List<Business> existing = businessRepository.findByOwner(owner);
        if (!existing.isEmpty()) return existing.get(0);
        return businessRepository.save(new Business(owner, name, location, serviceType));
    }

    public Queue createQueue(Business business, String serviceName, int avgTime) {
        return queueRepository.save(new Queue(business, serviceName, avgTime));
    }

    public List<Queue> listQueuesWithEntries(Business business) {
        return queueRepository.findByBusiness(business);
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
