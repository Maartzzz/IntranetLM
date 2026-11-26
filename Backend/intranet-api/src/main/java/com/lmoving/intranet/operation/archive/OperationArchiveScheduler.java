package com.lmoving.intranet.operation.archive;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OperationArchiveScheduler {
    private final OperationArchiveService service;
    public OperationArchiveScheduler(OperationArchiveService service) { this.service = service; }

    @Scheduled(cron = "0 15 2 * * *")
    public void archiveDaily() { service.archiveEligible(); }
}
