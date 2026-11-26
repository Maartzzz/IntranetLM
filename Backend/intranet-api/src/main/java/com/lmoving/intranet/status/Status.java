package com.lmoving.intranet.status;

import jakarta.persistence.*;

@Entity
@Table(name = "statuses", uniqueConstraints = {
    @UniqueConstraint(name = "uq_statuses_process_name", columnNames = {"process_type_id", "name"})
})
public class Status {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "process_type_id", nullable = false) private StatusProcessType processType;
    @Column(nullable = false, length = 80) private String name;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(length = 20) private String color;
    @Column(nullable = false) private boolean active = true;
    @Column(name = "is_initial", nullable = false) private boolean initial;
    @Column(name = "is_final", nullable = false) private boolean finalStatus;
    @Column(name = "auto_transition", nullable = false) private boolean autoTransition;
    @Column(name = "allows_scheduling", nullable = false) private boolean allowsScheduling;

    public Status() {}
    public Long getId() { return id; }
    public StatusProcessType getProcessType() { return processType; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getColor() { return color; }
    public boolean isActive() { return active; }
    public boolean isInitial() { return initial; }
    public boolean isFinalStatus() { return finalStatus; }
    public boolean isAutoTransition() { return autoTransition; }
    public boolean isAllowsScheduling() { return allowsScheduling; }
    public void setName(String value) { name = value; }
    public void setDescription(String value) { description = value; }
    public void setColor(String value) { color = value; }
    public void setActive(boolean value) { active = value; }
    public void setInitial(boolean value) { initial = value; }
    public void setFinalStatus(boolean value) { finalStatus = value; }
    public void setAutoTransition(boolean value) { autoTransition = value; }
    public void setAllowsScheduling(boolean value) { allowsScheduling = value; }
    public void setProcessType(StatusProcessType value) { processType = value; }
}
