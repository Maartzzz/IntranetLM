package com.lmoving.intranet.driver;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverRepository
        extends JpaRepository<Driver, Long> {

    Optional<Driver> findByUserUsername(String username);

    boolean existsByUserUsername(String username);

    boolean existsByLicenseNumber(String licenseNumber);

}