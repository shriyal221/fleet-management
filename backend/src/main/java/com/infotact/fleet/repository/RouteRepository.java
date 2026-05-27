package com.infotact.fleet.repository;

import com.infotact.fleet.domain.Route;
import com.infotact.fleet.domain.RouteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findAllByStatus(RouteStatus status);
    List<Route> findAllByDriverId(Long driverId);
    boolean existsByVehicleIdAndStatus(Long vehicleId, RouteStatus status);
    boolean existsByDriverIdAndStatus(Long driverId, RouteStatus status);
    long countByRouteNameStartingWith(String prefix);
}
