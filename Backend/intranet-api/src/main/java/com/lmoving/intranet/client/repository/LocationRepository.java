package com.lmoving.intranet.client.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lmoving.intranet.client.Location;
public interface LocationRepository extends JpaRepository<Location, Long> {
 List<Location> findAllByActiveTrueOrderByNameAsc();
 List<Location> findAllByOrderByNameAsc();
}
