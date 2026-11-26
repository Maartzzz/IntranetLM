package com.lmoving.intranet.client.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.lmoving.intranet.client.ClientLocation;
import com.lmoving.intranet.client.LocationType;
public record ClientLocationResponse(Long id, String name, LocationType locationType, String address, String reference, String district, String province, String department, BigDecimal latitude, BigDecimal longitude, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
 public static ClientLocationResponse from(ClientLocation link) { var l=link.getLocation(); return new ClientLocationResponse(l.getId(),l.getName(),l.getLocationType(),l.getAddress(),l.getReference(),l.getDistrict(),l.getProvince(),l.getDepartment(),l.getLatitude(),l.getLongitude(),link.isActive() && l.isActive(),l.getCreatedAt(),l.getUpdatedAt()); }
}
