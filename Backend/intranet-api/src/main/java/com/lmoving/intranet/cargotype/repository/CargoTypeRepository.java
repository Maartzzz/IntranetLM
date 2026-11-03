package com.lmoving.intranet.cargotype.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lmoving.intranet.cargotype.CargoType;

public interface CargoTypeRepository extends JpaRepository<CargoType, Long> {

    List<CargoType> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);
}
