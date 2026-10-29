package com.lmoving.intranet.cic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lmoving.intranet.cic.CicDocumento;
import com.lmoving.intranet.cic.CicEstadoDocumento;

public interface CicDocumentoRepository
        extends JpaRepository<CicDocumento, Long> {

    List<CicDocumento>
        findByEstadoOrderByPublicadoEnDesc(
            CicEstadoDocumento estado
        );

    List<CicDocumento>
        findByCategoria_IdAndEstadoOrderByPublicadoEnDesc(
            Long categoriaId,
            CicEstadoDocumento estado
        );

    List<CicDocumento>
        findAllByOrderByCreadoEnDesc();
}
