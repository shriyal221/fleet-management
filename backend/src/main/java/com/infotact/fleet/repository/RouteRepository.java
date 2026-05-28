package com.infotact.fleet.repository;

import com.infotact.fleet.domain.Route;
import com.infotact.fleet.domain.RouteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findAllByStatus(RouteStatus status);
    List<Route> findAllByDriverId(Long driverId);
    boolean existsByVehicleIdAndStatus(Long vehicleId, RouteStatus status);
    boolean existsByDriverIdAndStatus(Long driverId, RouteStatus status);
    long countByRouteNameStartingWith(String prefix);

    @Query("SELECT r FROM Route r WHERE (:status IS NULL OR r.status = :status) " +
           "AND (:search IS NULL OR LOWER(r.routeName) LIKE :search " +
           "OR LOWER(r.driver.name) LIKE :search " +
           "OR LOWER(r.vehicle.licensePlate) LIKE :search)")
    Page<Route> searchRoutes(@Param("status") RouteStatus status, @Param("search") String search, Pageable pageable);
}
