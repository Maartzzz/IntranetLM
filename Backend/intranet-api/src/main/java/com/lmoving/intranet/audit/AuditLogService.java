package com.lmoving.intranet.audit;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.lmoving.intranet.user.User;
import com.lmoving.intranet.user.UserRepository;

@Service
@Transactional
public class AuditLogService {
    private final AuditLogRepository repository;
    private final UserRepository userRepository;
    public AuditLogService(AuditLogRepository repository, UserRepository userRepository) {
        this.repository=repository; this.userRepository=userRepository;
    }
    public void registrar(String module, String entityType, Long entityId, String action,
            String description, String oldValue, String newValue) {
        registrar(module, entityType, entityId, null, action, description, oldValue, newValue);
    }
    public void registrar(String module, String entityType, Long entityId, String entityKey, String action,
            String description, String oldValue, String newValue) {
        Authentication auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth==null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se pudo identificar al usuario de la sesión");
        registrarComoUsuario(auth.getName(), module, entityType, entityId, entityKey,
                action, description, oldValue, newValue);
    }
    /** Registra una acción ejecutada por un usuario autenticado identificado por el controlador. */
    public void registrarComoUsuario(String username, String module, String entityType, Long entityId,
            String action, String description, String oldValue, String newValue) {
        registrarComoUsuario(username, module, entityType, entityId, null,
                action, description, oldValue, newValue);
    }

    /** Variante que permite asociar el evento a una clave de entidad cuando no existe un ID numérico. */
    public void registrarComoUsuario(String username, String module, String entityType, Long entityId,
            String entityKey, String action, String description, String oldValue, String newValue) {
        if (username == null || username.isBlank())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se pudo identificar al usuario de la sesión");
        User user = userRepository.findByUsername(username).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario autenticado no encontrado"));
        String name = ((user.getFirstName() == null ? "" : user.getFirstName()) + " " +
            (user.getLastName() == null ? "" : user.getLastName())).trim();
        AuditLog event = new AuditLog();
        event.setActorUsername(username);
        event.setActorName(name.isBlank() ? username : name);
        event.setModule(module);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setEntityKey(entityKey);
        event.setAction(action);
        event.setDescription(description);
        event.setOldValue(oldValue);
        event.setNewValue(newValue);
        repository.save(event);
    }

    /** Registra intentos de autenticación sin almacenar contraseñas ni detalles de credenciales. */
    public void registrarAutenticacion(String username, boolean exitoso) {
        String attemptedUsername = limitar(username, 50);
        if (attemptedUsername.isBlank()) {
            attemptedUsername = "UNKNOWN";
        }

        String actorName;
        if (exitoso) {
            User user = userRepository.findByUsername(attemptedUsername).orElse(null);
            String fullName = user == null ? "" : ((user.getFirstName() == null ? "" : user.getFirstName()) + " " +
                    (user.getLastName() == null ? "" : user.getLastName())).trim();
            actorName = limitar(fullName.isBlank() ? attemptedUsername : fullName, 201);
        } else {
            actorName = limitar("Intento de acceso: " + attemptedUsername, 201);
        }

        AuditLog event = new AuditLog();
        event.setActorUsername(attemptedUsername);
        event.setActorName(actorName);
        event.setModule("SEGURIDAD");
        event.setEntityType("AUTENTICACION");
        event.setEntityKey(limitar(username, 100));
        event.setAction(exitoso ? "LOGIN_SUCCESS" : "LOGIN_FAILURE");
        event.setDescription(exitoso ? "Inicio de sesión exitoso" : "Intento de inicio de sesión fallido");
        event.setNewValue(exitoso ? "SUCCESS" : "FAILURE");
        repository.save(event);
    }

    private String limitar(String value, int maxLength) {
        if (value == null) return "";
        String normalized = value.trim();
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength);
    }

    public void registrarSistema(String module, String entityType, Long entityId, String action,
            String description, String oldValue, String newValue) {
        AuditLog event = new AuditLog();
        event.setActorUsername("SYSTEM");
        event.setActorName("Sistema automático");
        event.setModule(module); event.setEntityType(entityType); event.setEntityId(entityId);
        event.setAction(action); event.setDescription(description);
        event.setOldValue(oldValue); event.setNewValue(newValue);
        repository.save(event);
    }

    @Transactional(readOnly=true)
    public List<AuditLogResponse> listar(String module, String entityType, Long entityId, String entityKey) {
        List<AuditLog> rows;
        if(entityType!=null && entityId!=null)
            rows=repository.findTop500ByEntityTypeAndEntityIdOrderByCreatedAtDescIdDesc(entityType,entityId);
        else if(entityType!=null && entityKey!=null && !entityKey.isBlank())
            rows=repository.findTop500ByEntityTypeAndEntityKeyOrderByCreatedAtDescIdDesc(entityType,entityKey);
        else if(module!=null && !module.isBlank())
            rows=repository.findTop500ByModuleOrderByCreatedAtDescIdDesc(module);
        else rows=repository.findTop500ByOrderByCreatedAtDescIdDesc();
        return rows.stream().map(x -> new AuditLogResponse(x.getId(),x.getActorUsername(),
            x.getActorName(),x.getModule(),x.getEntityType(),x.getEntityId(),x.getEntityKey(),x.getAction(),
            x.getDescription(),x.getOldValue(),x.getNewValue(),x.getCreatedAt())).toList();
    }
}
