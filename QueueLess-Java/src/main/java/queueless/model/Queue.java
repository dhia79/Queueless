package queueless.model;

import jakarta.persistence.*;

@Entity
@Table(name = "queues")
public class Queue extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "avg_service_time")
    private int avgServiceTime;

    @Column(name = "is_active")
    private boolean active = true;

    public Queue() {}

    public Queue(Business business, String serviceName, int avgServiceTime) {
        this.business = business;
        this.serviceName = serviceName;
        this.avgServiceTime = avgServiceTime;
    }

    public Business getBusiness() { return business; }
    public void setBusiness(Business business) { this.business = business; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public int getAvgServiceTime() { return avgServiceTime; }
    public void setAvgServiceTime(int avgServiceTime) { this.avgServiceTime = avgServiceTime; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public void display() {
        System.out.println("Queue: " + serviceName + " (" + avgServiceTime + " min)");
    }
}
