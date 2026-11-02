package com.lmoving.intranet.activity;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.lmoving.intranet.activity.dto.OperationActivityResponse;
import com.lmoving.intranet.operation.Operation;
import com.lmoving.intranet.operation.OperationRepository;
import com.lmoving.intranet.user.User;
import com.lmoving.intranet.user.UserRepository;

@Service
@Transactional
public class OperationActivityService {
    private final OperationActivityRepository activityRepository;
    private final OperationRepository operationRepository;
    private final UserRepository userRepository;

    public OperationActivityService(OperationActivityRepository activityRepository,
            OperationRepository operationRepository, UserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.operationRepository = operationRepository;
        this.userRepository = userRepository;
    }

    public void registrar(Long operationId, String action, String entityType, Long entityId,
            String description, String oldValue, String newValue) {
        Operation operation = operationRepository.findById(operationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Operación no encontrada"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se pudo identificar al usuario de la sesión");
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario autenticado no encontrado"));
        String fullName = ((user.getFirstName() == null ? "" : user.getFirstName()) + " " +
            (user.getLastName() == null ? "" : user.getLastName())).trim();
        OperationActivity event = new OperationActivity();
        event.setOperation(operation);
        event.setActorUsername(username);
        event.setActorName(fullName.isBlank() ? username : fullName);
        event.setAction(action);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setDescription(description);
        event.setOldValue(oldValue);
        event.setNewValue(newValue);
        activityRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<OperationActivityResponse> listar(Long operationId) {
        if (!operationRepository.existsById(operationId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Operación no encontrada");
        return activityRepository.findAllByOperation_IdOrderByCreatedAtDescIdDesc(operationId).stream()
            .map(x -> new OperationActivityResponse(x.getId(), x.getOperation().getId(),
                x.getActorUsername(), x.getActorName(), x.getAction(), x.getEntityType(),
                x.getEntityId(), x.getDescription(), x.getOldValue(), x.getNewValue(), x.getCreatedAt()))
            .toList();
    }
}
