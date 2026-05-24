package com.infotact.fleet.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "routes", uniqueConstraints = {
    @UniqueConstraint(columnNames = "route_name")
})
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_name", nullable = false, length = 50)
    private String routeName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RouteStatus status = RouteStatus.PLANNED;

    @Column(name = "total_distance_km")
    private Double totalDistanceKm;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Column(name = "total_fuel_estimate_liters")
    private Double totalFuelEstimateLiters;

    @Column(name = "start_latitude", nullable = false)
    private Double startLatitude;

    @Column(name = "start_longitude", nullable = false)
    private Double startLongitude;

    @Column(name = "optimized_waypoint_order", columnDefinition = "TEXT")
    private String optimizedWaypointOrder;

    @Column(name = "dispatched_at")
    private Instant dispatchedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Route() {
        // Required by JPA
    }

    public Route(String routeName, Vehicle vehicle, Driver driver, Double startLatitude, Double startLongitude) {
        this.routeName = routeName;
        this.vehicle = vehicle;
        this.driver = driver;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.status = RouteStatus.PLANNED;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void setOptimizationResult(Double distanceKm, Integer durationMin, Double fuelLiters, String waypointOrderJson) {
        this.totalDistanceKm = distanceKm;
        this.estimatedDurationMinutes = durationMin;
        this.totalFuelEstimateLiters = fuelLiters;
        this.optimizedWaypointOrder = waypointOrderJson;
    }

    public void dispatch() {
        if (this.status != RouteStatus.PLANNED) {
            throw new IllegalStateException("Only PLANNED routes can be dispatched. Current status: " + this.status);
        }
        this.status = RouteStatus.ACTIVE;
        this.dispatchedAt = Instant.now();
    }

    public void complete() {
        if (this.status != RouteStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE routes can be completed. Current status: " + this.status);
        }
        this.status = RouteStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void cancel() {
        if (this.status == RouteStatus.COMPLETED) {
            throw new IllegalStateException("Completed routes cannot be cancelled.");
        }
        this.status = RouteStatus.CANCELLED;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getRouteName() { return routeName; }
    public Vehicle getVehicle() { return vehicle; }
    public Driver getDriver() { return driver; }
    public RouteStatus getStatus() { return status; }
    public Double getTotalDistanceKm() { return totalDistanceKm; }
    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public Double getTotalFuelEstimateLiters() { return totalFuelEstimateLiters; }
    public Double getStartLatitude() { return startLatitude; }
    public Double getStartLongitude() { return startLongitude; }
    public String getOptimizedWaypointOrder() { return optimizedWaypointOrder; }
    public Instant getDispatchedAt() { return dispatchedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
