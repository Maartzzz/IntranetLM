package com.lmoving.intranet.client.dto;
import com.lmoving.intranet.client.LocationUsageType;
import jakarta.validation.constraints.*;
public record ClientSubclientLocationRequest(@NotNull Long locationId, @NotNull LocationUsageType usageType, @Size(max=150) String contactName, @Size(max=30) String contactPhone, @Size(max=200) String operatingHours, String instructions) {}
