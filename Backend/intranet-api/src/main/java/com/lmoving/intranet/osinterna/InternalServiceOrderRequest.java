package com.lmoving.intranet.osinterna;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
public record InternalServiceOrderRequest(
 @NotNull Long clientId, Long clientServiceOrderId,
 @NotBlank @Pattern(regexp="FCL|LCL") String serviceType,
 @Size(max=120) String reference, @NotNull Long originLocationId, @NotNull Long destinationLocationId,
 Long providerId, LocalDate scheduledDate, Long statusId,
 @Pattern(regexp="PENDIENTE|FACTURADO") String billingStatus, String observations,
 @Valid FclDetail fclDetail, @Valid LclDetail lclDetail
){
 public record FclDetail(@Size(max=40) String containerNumber, Long cargoTypeId,
  @DecimalMin("0.0") BigDecimal cubicMeters, Long emptyWarehouseLocationId, LocalDate returnDueDate, String memo){}
 public record LclDetail(@Min(0) Integer packages, @DecimalMin("0.0") BigDecimal weightKg, @DecimalMin("0.0") BigDecimal cubicMeters){}
}
