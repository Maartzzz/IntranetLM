package com.lmoving.intranet.osinterna.schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.lmoving.intranet.osinterna.InternalServiceOrder;

import jakarta.persistence.*;

@Entity
@Table(
    name = "internal_service_order_schedule_exclusions",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_ios_exclusion_order_date",
            columnNames = {
                "internal_service_order_id",
                "excluded_date"
            }
        )
    }
)
public class InternalServiceOrderScheduleExclusion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "internal_service_order_id", nullable = false)
    private InternalServiceOrder internalServiceOrder;

    @Column(name = "excluded_date", nullable = false)
    private LocalDate excludedDate;

    @Column(
        name = "created_at",
        nullable = false,
        insertable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    public InternalServiceOrderScheduleExclusion() {}

    public InternalServiceOrderScheduleExclusion(
        InternalServiceOrder internalServiceOrder,
        LocalDate excludedDate
    ) {
        this.internalServiceOrder = internalServiceOrder;
        this.excludedDate = excludedDate;
    }

    public Long getId() {
        return id;
    }

    public InternalServiceOrder getInternalServiceOrder() {
        return internalServiceOrder;
    }

    public void setInternalServiceOrder(InternalServiceOrder value) {
        this.internalServiceOrder = value;
    }

    public LocalDate getExcludedDate() {
        return excludedDate;
    }

    public void setExcludedDate(LocalDate value) {
        this.excludedDate = value;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}