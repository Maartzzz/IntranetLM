package com.lmoving.intranet.osinterna;
import java.time.LocalDate;
import java.time.LocalDateTime;
public record InternalServiceOrderResponse(Long id, Long internalOrderNumber, Long clientId, String clientName,
 Long clientServiceOrderId, String clientOrderNumber, String serviceType, String reference,
 Long originLocationId, String originName, Long destinationLocationId, String destinationName,
 Long providerId, String providerName, LocalDate scheduledDate, Long statusId, String statusName, String billingStatus,
 String observations, Object detail, LocalDateTime createdAt, LocalDateTime updatedAt) {}
