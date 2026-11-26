package com.lmoving.intranet.osinterna.schedule;

import java.time.LocalDate;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.lmoving.intranet.audit.AuditLogService;
import com.lmoving.intranet.osinterna.InternalServiceOrder;
import com.lmoving.intranet.osinterna.InternalServiceOrderRepository;
import com.lmoving.intranet.status.Status;
import com.lmoving.intranet.status.StatusRepository;

@Component
public class InternalServiceOrderStatusScheduler {
    private static final String PROCESS = "OS_INTERNA";
    private final InternalServiceOrderRepository orders;
    private final StatusRepository statuses;
    private final AuditLogService audit;

    public InternalServiceOrderStatusScheduler(InternalServiceOrderRepository orders,
            StatusRepository statuses, AuditLogService audit) {
        this.orders = orders;
        this.statuses = statuses;
        this.audit = audit;
    }

    @Scheduled(cron = "0 15 2 * * *", zone = "America/Lima")
    @Transactional
    public void marcarVencidas() {
        LocalDate today = LocalDate.now(java.time.ZoneId.of("America/Lima"));
        Status programmed = statuses.findFirstByProcessType_CodeAndNameAndActiveTrue(PROCESS, "Programada").orElse(null);
        Status overdue = statuses.findFirstByProcessType_CodeAndNameAndActiveTrue(PROCESS, "Vencida").orElse(null);
        if (programmed == null || overdue == null) return;

        List<InternalServiceOrder> pending =
            orders.findAllByScheduledDateBeforeAndActiveTrueAndStatus_Id(today, programmed.getId());

        for (InternalServiceOrder order : pending) {
            order.setStatus(overdue);
            orders.save(order);
            audit.registrar("PROGRAMACION", "OS_INTERNA", order.getId(), "STATUS_AUTO_OVERDUE",
                "La O/S interna pasó automáticamente a Vencida porque su fecha programada ya transcurrió",
                programmed.getName(), overdue.getName());
        }
    }
}
