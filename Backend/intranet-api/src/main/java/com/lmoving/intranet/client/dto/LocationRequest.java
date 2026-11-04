package com.lmoving.intranet.client.dto;
import java.math.BigDecimal;
import com.lmoving.intranet.client.LocationType;
import jakarta.validation.constraints.*;
public record LocationRequest(
 @NotBlank @Size(max=150) String name, @NotNull LocationType locationType, @NotBlank @Size(max=300) String address,
 @Size(max=300) String reference, @Size(max=100) String district, @Size(max=100) String province, @Size(max=100) String department,
 @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude, @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude) {}
