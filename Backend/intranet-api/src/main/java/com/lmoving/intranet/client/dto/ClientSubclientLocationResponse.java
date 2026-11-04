package com.lmoving.intranet.client.dto;
import java.time.LocalDateTime;
import com.lmoving.intranet.client.ClientSubclientLocation;
import com.lmoving.intranet.client.LocationUsageType;
public record ClientSubclientLocationResponse(Long id, Long clientSubclientId, Long locationId, String locationName, String address, String locationType, LocationUsageType usageType, String contactName, String contactPhone, String operatingHours, String instructions, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
 public static ClientSubclientLocationResponse from(ClientSubclientLocation r) { var l=r.getLocation(); return new ClientSubclientLocationResponse(r.getId(),r.getClientSubclient().getId(),l.getId(),l.getName(),l.getAddress(),l.getLocationType().name(),r.getUsageType(),r.getContactName(),r.getContactPhone(),r.getOperatingHours(),r.getInstructions(),r.isActive(),r.getCreatedAt(),r.getUpdatedAt()); }
}
