package queueless.model;

import jakarta.persistence.*;

@Entity
@Table(name = "businesses")
public class Business extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User owner;

    private String name;
    private String location;

    @Column(name = "service_type")
    private String serviceType;

    public Business() {}

    public Business(User owner, String name, String location, String serviceType) {
        this.owner = owner;
        this.name = name;
        this.location = location;
        this.serviceType = serviceType;
    }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    @Override
    public void display() {
        System.out.println("Business: " + name + " at " + location);
    }
}
