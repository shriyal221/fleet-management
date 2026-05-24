package com.infotact.fleet.repository;

import com.infotact.fleet.domain.Driver;
import com.infotact.fleet.domain.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByLicenseNumber(String licenseNumber);
    boolean existsByLicenseNumber(String licenseNumber);
    List<Driver> findAllByStatus(DriverStatus status);
}
