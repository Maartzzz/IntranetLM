package com.lmoving.intranet.operation.archive;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OperationArchiveResponse(
    Long id, String operationNumber, String reference, String clientName,
    String clientOrderNumber, int internalOrderCount, List<String> internalOrderNumbers,
    String cargoType, boolean container, LocalDate estimatedDate, String finalStatus,
    LocalDateTime archivedAt, String reason
) {}
