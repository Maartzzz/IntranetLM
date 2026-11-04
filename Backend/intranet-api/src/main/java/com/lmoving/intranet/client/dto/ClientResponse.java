package com.lmoving.intranet.client.dto;

import java.time.LocalDateTime;

import com.lmoving.intranet.client.Client;
import com.lmoving.intranet.client.ClientDocumentType;

public record ClientResponse(
    Long id,
    ClientDocumentType documentType,
    String documentNumber,
    String businessName,
    String address,
    boolean active,
    Long parentClientId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ClientResponse from(Client client) {
        return new ClientResponse(
            client.getId(), client.getDocumentType(), client.getDocumentNumber(),
            client.getBusinessName(), client.getAddress(), client.isActive(), null,
            client.getCreatedAt(), client.getUpdatedAt()
        );
    }
}
