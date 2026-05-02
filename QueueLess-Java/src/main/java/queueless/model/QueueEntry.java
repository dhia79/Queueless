package queueless.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "queue_entries")
public class QueueEntry extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "queue_id", nullable = false)
    private Queue queue;

    private int position;

    @Enumerated(EnumType.STRING)
    private EntryStatus status;

    @Column(name = "join_time")
    private LocalDateTime joinTime;

    public QueueEntry() {}

    public QueueEntry(User user, Queue queue, int position) {
        this.user = user;
        this.queue = queue;
        this.position = position;
        this.status = EntryStatus.WAITING;
        this.joinTime = LocalDateTime.now();
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Queue getQueue() { return queue; }
    public void setQueue(Queue queue) { this.queue = queue; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public EntryStatus getStatus() { return status; }
    public void setStatus(EntryStatus status) { this.status = status; }
    public LocalDateTime getJoinTime() { return joinTime; }
    public void setJoinTime(LocalDateTime joinTime) { this.joinTime = joinTime; }

    @Override
    public void display() {
        System.out.println("Entry: User " + user.getName() + " in " + queue.getServiceName());
    }
}
