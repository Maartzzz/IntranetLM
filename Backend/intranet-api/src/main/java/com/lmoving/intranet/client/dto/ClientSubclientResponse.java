package com.lmoving.intranet.client.dto;
import java.time.LocalDateTime;
import com.lmoving.intranet.client.ClientSubclient;
public record ClientSubclientResponse(Long id, Long clientId, String clientName, Long subclientId, String subclientName, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
 public static ClientSubclientResponse from(ClientSubclient r) { return new ClientSubclientResponse(r.getId(), r.getClient().getId(), r.getClient().getBusinessName(), r.getSubclient().getId(), r.getSubclient().getBusinessName(), r.isActive(), r.getCreatedAt(), r.getUpdatedAt()); }
}
