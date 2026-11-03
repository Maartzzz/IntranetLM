package com.lmoving.intranet.client.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lmoving.intranet.client.Client;
import com.lmoving.intranet.client.ClientDocumentType;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findAllByOrderByBusinessNameAsc();
    List<Client> findAllByActiveTrueOrderByBusinessNameAsc();
    Optional<Client> findByDocumentTypeAndDocumentNumber(ClientDocumentType documentType, String documentNumber);
    boolean existsByDocumentTypeAndDocumentNumber(ClientDocumentType documentType, String documentNumber);
}
