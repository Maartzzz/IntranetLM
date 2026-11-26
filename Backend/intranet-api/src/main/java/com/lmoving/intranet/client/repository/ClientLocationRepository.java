package com.lmoving.intranet.client.repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lmoving.intranet.client.ClientLocation;
public interface ClientLocationRepository extends JpaRepository<ClientLocation,Long> {
 List<ClientLocation> findAllByClient_IdOrderByLocation_NameAsc(Long clientId);
 Optional<ClientLocation> findByClient_IdAndLocation_Id(Long clientId, Long locationId);
 boolean existsByLocation_Id(Long locationId);
}
