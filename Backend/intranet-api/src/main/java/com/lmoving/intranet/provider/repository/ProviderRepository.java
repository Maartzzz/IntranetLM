package com.lmoving.intranet.provider.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lmoving.intranet.provider.Provider;
import com.lmoving.intranet.provider.ProviderDocumentType;

public interface ProviderRepository extends JpaRepository<Provider, Long> {
    List<Provider> findAllByActiveTrueOrderByBusinessNameAsc();
    List<Provider> findAllByOrderByBusinessNameAsc();
    Optional<Provider> findByDocumentTypeAndDocumentNumber(ProviderDocumentType documentType, String documentNumber);
    boolean existsByDocumentTypeAndDocumentNumber(ProviderDocumentType documentType, String documentNumber);
    boolean existsByDocumentTypeAndDocumentNumberAndIdNot(ProviderDocumentType documentType, String documentNumber, Long id);
}
