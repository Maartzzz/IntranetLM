package com.lmoving.intranet.cic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lmoving.intranet.cic.CicCategoria;

public interface CicCategoriaRepository
        extends JpaRepository<CicCategoria, Long> {

    List<CicCategoria> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);
}
