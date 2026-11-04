package com.lmoving.intranet.client.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.lmoving.intranet.client.Location;
import com.lmoving.intranet.client.LocationType;
public record LocationResponse(Long id, String name, LocationType locationType, String address, String reference, String district, String province, String department, BigDecimal latitude, BigDecimal longitude, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
 public static LocationResponse from(Location l) { return new LocationResponse(l.getId(),l.getName(),l.getLocationType(),l.getAddress(),l.getReference(),l.getDistrict(),l.getProvince(),l.getDepartment(),l.getLatitude(),l.getLongitude(),l.isActive(),l.getCreatedAt(),l.getUpdatedAt()); }
}
