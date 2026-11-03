package com.lmoving.intranet.status;

import jakarta.persistence.*;

@Entity
@Table(name = "statuses", uniqueConstraints = {
    @UniqueConstraint(name = "uq_statuses_process_name", columnNames = {"process_type_id", "name"})
})
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "process_type_id", nullable = false)
    private StatusProcessType processType;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 20)
    private String color;

    @Column(nullable = false)
    private boolean active = true;

    public Status() {}
    public Long getId() { return id; }
    public StatusProcessType getProcessType() { return processType; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getColor() { return color; }
    public boolean isActive() { return active; }
}
