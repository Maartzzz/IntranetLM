package com.lmoving.intranet.client.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lmoving.intranet.client.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    List<Warehouse> findAllByActiveTrueOrderByWarehouseNameAsc();
    List<Warehouse> findAllByOrderByWarehouseNameAsc();
    boolean existsByWarehouseCodeIgnoreCase(String warehouseCode);
    boolean existsByWarehouseCodeIgnoreCaseAndIdNot(String warehouseCode, Long id);
    boolean existsByLocationId(Long locationId);
}
