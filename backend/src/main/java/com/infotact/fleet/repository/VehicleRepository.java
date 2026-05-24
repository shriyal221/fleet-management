package com.infotact.fleet.repository;

import com.infotact.fleet.domain.Vehicle;
import com.infotact.fleet.domain.VehicleMaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    boolean existsByLicensePlate(String licensePlate);
    List<Vehicle> findAllByMaintenanceStatus(VehicleMaintenanceStatus status);
}
