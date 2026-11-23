package com.lmoving.intranet.audit;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop500ByOrderByCreatedAtDescIdDesc();
    List<AuditLog> findTop500ByModuleOrderByCreatedAtDescIdDesc(String module);
    List<AuditLog> findTop500ByEntityTypeAndEntityIdOrderByCreatedAtDescIdDesc(String entityType, Long entityId);
    List<AuditLog> findTop500ByEntityTypeAndEntityKeyOrderByCreatedAtDescIdDesc(String entityType, String entityKey);
}
