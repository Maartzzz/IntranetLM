package com.lmoving.intranet.requirement;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementCatalogRepository extends JpaRepository<RequirementCatalog, Long> {
    List<RequirementCatalog> findAllByOrderByNameAsc();
    List<RequirementCatalog> findAllByActiveTrueOrderByNameAsc();
    boolean existsByNameIgnoreCase(String name);
}
