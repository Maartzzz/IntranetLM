package com.lmoving.intranet.materials.dto;

import java.time.LocalDateTime;

public class CourseMaterialProgressResponse {

    private Long id;

    private Long courseMaterialId;

    private String username;

    private LocalDateTime startedAt;

    private LocalDateTime lastAccessedAt;

    private LocalDateTime completedAt;

    private Integer progressPercent;

    private Long lastPosition;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseMaterialId() {
        return courseMaterialId;
    }

    public void setCourseMaterialId(Long courseMaterialId) {
        this.courseMaterialId = courseMaterialId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Integer progressPercent) {
        this.progressPercent = progressPercent;
    }

    public Long getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(Long lastPosition) {
        this.lastPosition = lastPosition;
    }
}