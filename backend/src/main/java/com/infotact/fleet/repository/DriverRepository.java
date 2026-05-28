package com.infotact.fleet.repository;

import com.infotact.fleet.domain.Driver;
import com.infotact.fleet.domain.DriverStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByLicenseNumber(String licenseNumber);
    boolean existsByLicenseNumber(String licenseNumber);
    List<Driver> findAllByStatus(DriverStatus status);

    @Query("SELECT d FROM Driver d WHERE (:status IS NULL OR d.status = :status) " +
           "AND (:search IS NULL OR LOWER(d.name) LIKE :search " +
           "OR LOWER(d.licenseNumber) LIKE :search " +
           "OR LOWER(d.email) LIKE :search)")
    Page<Driver> searchDrivers(@Param("status") DriverStatus status, @Param("search") String search, Pageable pageable);
}
