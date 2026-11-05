package com.lmoving.intranet.osinterna.schedule.dto;

import java.time.LocalDate;

public record ScheduleOrderResponse(
    Long id,
    Long internalOrderNumber,
    Long clientId,
    String clientName,
    Long clientServiceOrderId,
    String clientOrderNumber,
    String serviceType,
    String reference,
    Long originLocationId,
    String originName,
    Long destinationLocationId,
    String destinationName,
    Long providerId,
    String providerName,
    LocalDate scheduledDate,
    Long statusId,
    String statusName,
    String billingStatus,
    String observations,
    ScheduleFclDetailResponse fclDetail,
    ScheduleLclDetailResponse lclDetail
) {}