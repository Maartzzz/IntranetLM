package com.lmoving.intranet.client.repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lmoving.intranet.client.ClientSubclientLocation;
public interface ClientSubclientLocationRepository extends JpaRepository<ClientSubclientLocation, Long> {
 List<ClientSubclientLocation> findAllByClientSubclient_IdAndActiveTrueOrderByLocation_NameAsc(Long relationId);
 Optional<ClientSubclientLocation> findByClientSubclient_IdAndLocation_Id(Long relationId, Long locationId);
}
