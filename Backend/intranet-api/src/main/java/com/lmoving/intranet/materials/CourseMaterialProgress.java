package com.lmoving.intranet.materials;

import com.lmoving.intranet.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "course_material_progress",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_material_progress_user_material",
            columnNames = {
                "course_material_id",
                "username"
            }
        )
    }
)
public class CourseMaterialProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "course_material_id",
        nullable = false
    )
    private CourseMaterial courseMaterial;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "username",
        nullable = false
    )
    private User user;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime lastAccessedAt;

    private LocalDateTime completedAt;

    @Column(nullable = false)
    private Integer progressPercent = 0;

    private Long lastPosition;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        startedAt = now;
        lastAccessedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        lastAccessedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public CourseMaterial getCourseMaterial() {
        return courseMaterial;
    }

    public void setCourseMaterial(
            CourseMaterial courseMaterial
    ) {
        this.courseMaterial = courseMaterial;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(
            LocalDateTime completedAt
    ) {
        this.completedAt = completedAt;
    }

    public Integer getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(
            Integer progressPercent
    ) {
        this.progressPercent = progressPercent;
    }

    public Long getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(
            Long lastPosition
    ) {
        this.lastPosition = lastPosition;
    }
}