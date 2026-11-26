package com.lmoving.intranet.authorization.repository;

import com.lmoving.intranet.authorization.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {
    List<PermissionEntity> findByActiveTrueOrderByCodeAsc();
    boolean existsByCodeAndActiveTrue(String code);
}
