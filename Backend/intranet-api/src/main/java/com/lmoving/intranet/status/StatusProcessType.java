package com.lmoving.intranet.status;

import jakarta.persistence.*;

@Entity
@Table(name = "process_types")
public class StatusProcessType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    public StatusProcessType() {}
    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
}
