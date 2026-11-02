package com.lmoving.intranet.status;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Long> {
    List<Status> findAllByProcessType_CodeAndActiveTrueOrderByIdAsc(String code);
    Optional<Status> findByIdAndProcessType_CodeAndActiveTrue(Long id, String code);
    Optional<Status> findFirstByProcessType_CodeAndNameAndActiveTrue(String code, String name);
}
