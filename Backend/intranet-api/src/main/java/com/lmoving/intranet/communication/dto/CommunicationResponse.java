
package com.lmoving.intranet.communication.dto;

import java.time.LocalDateTime;

import com.lmoving.intranet.communication.CommunicationStatus;

public class CommunicationResponse {

    private final Long id;
    private final LocalDateTime createdAt;
    private final CommunicationStatus emailStatus;
    private final String message;

    public CommunicationResponse(
            Long id,
            LocalDateTime createdAt,
            CommunicationStatus emailStatus,
            String message) {

        this.id = id;
        this.createdAt = createdAt;
        this.emailStatus = emailStatus;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public CommunicationStatus getEmailStatus() {
        return emailStatus;
    }

    public String getMessage() {
        return message;
    }
}
