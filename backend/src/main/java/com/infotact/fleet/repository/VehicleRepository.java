package com.infotact.fleet.repository;

import com.infotact.fleet.domain.Vehicle;
import com.infotact.fleet.domain.VehicleMaintenanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    boolean existsByLicensePlate(String licensePlate);
    List<Vehicle> findAllByMaintenanceStatus(VehicleMaintenanceStatus status);

    @Query("SELECT v FROM Vehicle v WHERE (:status IS NULL OR v.maintenanceStatus = :status) " +
           "AND (:search IS NULL OR LOWER(v.licensePlate) LIKE :search " +
           "OR LOWER(v.make) LIKE :search " +
           "OR LOWER(v.model) LIKE :search)")
    Page<Vehicle> searchVehicles(@Param("status") VehicleMaintenanceStatus status, @Param("search") String search, Pageable pageable);
}
