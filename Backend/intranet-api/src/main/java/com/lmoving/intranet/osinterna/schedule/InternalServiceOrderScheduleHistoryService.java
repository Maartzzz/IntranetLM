package com.lmoving.intranet.osinterna.schedule;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.lmoving.intranet.osinterna.InternalServiceOrder;
import com.lmoving.intranet.osinterna.InternalServiceOrderRepository;
import com.lmoving.intranet.user.User;
import com.lmoving.intranet.user.UserRepository;

@Service
@Transactional
public class InternalServiceOrderScheduleHistoryService {
    private final InternalServiceOrderScheduleHistoryRepository historyRepository;
    private final InternalServiceOrderRepository orderRepository;
    private final UserRepository userRepository;

    public InternalServiceOrderScheduleHistoryService(InternalServiceOrderScheduleHistoryRepository historyRepository,
            InternalServiceOrderRepository orderRepository, UserRepository userRepository) {
        this.historyRepository = historyRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public void registrar(InternalServiceOrder order, String eventType, LocalDate previousDate,
            LocalDate newDate, String reason) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se pudo identificar al usuario de la sesión");
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario autenticado no encontrado"));
        String fullName = ((user.getFirstName() == null ? "" : user.getFirstName()) + " " +
            (user.getLastName() == null ? "" : user.getLastName())).trim();
        String normalizedReason = reason == null || reason.isBlank() ? null : reason.trim();
        if (normalizedReason == null)
            throw new IllegalArgumentException("El motivo del evento de programación es obligatorio");
        historyRepository.save(new InternalServiceOrderScheduleHistory(order, eventType,
            previousDate, newDate, normalizedReason, username, fullName.isBlank() ? username : fullName));
    }

    @Transactional(readOnly = true)
    public List<InternalServiceOrderScheduleHistoryResponse> listar(Long orderId) {
        if (!orderRepository.existsById(orderId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "O/S interna no encontrada");
        return historyRepository.findAllByInternalServiceOrder_IdOrderByCreatedAtDescIdDesc(orderId)
            .stream().map(x -> new InternalServiceOrderScheduleHistoryResponse(x.getId(),
                x.getInternalServiceOrderId(), x.getEventType(), x.getPreviousDate(), x.getNewDate(),
                x.getReason(), x.getActorUsername(), x.getActorName(), x.getCreatedAt())).toList();
    }
}
