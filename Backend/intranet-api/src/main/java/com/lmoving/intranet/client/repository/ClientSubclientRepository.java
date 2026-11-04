package com.lmoving.intranet.client.repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lmoving.intranet.client.ClientSubclient;
public interface ClientSubclientRepository extends JpaRepository<ClientSubclient, Long> {
 List<ClientSubclient> findAllByClient_IdAndActiveTrueOrderBySubclient_BusinessNameAsc(Long clientId);
 List<ClientSubclient> findAllByClient_IdAndActiveTrue(Long clientId);
 Optional<ClientSubclient> findByClient_IdAndSubclient_Id(Long clientId, Long subclientId);
}
