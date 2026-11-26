package com.lmoving.intranet.status;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusProcessTypeRepository extends JpaRepository<StatusProcessType, Long> {
    Optional<StatusProcessType> findByCode(String code);
}
