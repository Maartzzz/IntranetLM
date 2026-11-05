package com.lmoving.intranet.osinterna.schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.lmoving.intranet.osinterna.InternalServiceOrder;
import jakarta.persistence.*;

@Entity
@Table(name = "internal_service_order_schedule_history")
public class InternalServiceOrderScheduleHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "internal_service_order_id", nullable = false)
    private InternalServiceOrder internalServiceOrder;
    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;
    @Column(name = "previous_date") private LocalDate previousDate;
    @Column(name = "new_date") private LocalDate newDate;
    @Column(nullable = false, columnDefinition = "TEXT") private String reason;
    @Column(name = "actor_username", nullable = false, length = 50) private String actorUsername;
    @Column(name = "actor_name", nullable = false, length = 210) private String actorName;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;

    protected InternalServiceOrderScheduleHistory() {}
    public InternalServiceOrderScheduleHistory(InternalServiceOrder order, String eventType,
            LocalDate previousDate, LocalDate newDate, String reason,
            String actorUsername, String actorName) {
        this.internalServiceOrder = order; this.eventType = eventType;
        this.previousDate = previousDate; this.newDate = newDate; this.reason = reason;
        this.actorUsername = actorUsername; this.actorName = actorName;
    }
    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Long getInternalServiceOrderId() { return internalServiceOrder.getId(); }
    public String getEventType() { return eventType; }
    public LocalDate getPreviousDate() { return previousDate; }
    public LocalDate getNewDate() { return newDate; }
    public String getReason() { return reason; }
    public String getActorUsername() { return actorUsername; }
    public String getActorName() { return actorName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
