package com.infotact.fleet.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "vehicles", uniqueConstraints = {
    @UniqueConstraint(columnNames = "license_plate")
})
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_plate", nullable = false, length = 30)
    private String licensePlate;

    @Column(nullable = false, length = 50)
    private String make;

    @Column(nullable = false, length = 50)
    private String model;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "capacity_kg", nullable = false)
    private Double capacityKg;

    @Column(name = "capacity_volume_cbm")
    private Double capacityVolumeCbm;

    @Column(name = "fuel_type", nullable = false, length = 20)
    private String fuelType;

    @Column(name = "current_odometer_km")
    private Double currentOdometerKm = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "maintenance_status", nullable = false, length = 30)
    private VehicleMaintenanceStatus maintenanceStatus = VehicleMaintenanceStatus.OPERATIONAL;

    @Column(name = "last_maintenance_date")
    private Instant lastMaintenanceDate;

    @Column(name = "next_maintenance_due_date")
    private Instant nextMaintenanceDueDate;

    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Vehicle() {
        // Required by JPA
    }

    public Vehicle(String licensePlate, String make, String model, Integer year, Double capacityKg, Double capacityVolumeCbm, String fuelType) {
        this.licensePlate = licensePlate;
        this.make = make;
        this.model = model;
        this.year = year;
        this.capacityKg = capacityKg;
        this.capacityVolumeCbm = capacityVolumeCbm;
        this.fuelType = fuelType;
        this.maintenanceStatus = VehicleMaintenanceStatus.OPERATIONAL;
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

    public void updateDetails(String make, String model, Integer year, Double capacityKg, Double capacityVolumeCbm, String fuelType) {
        this.make = make;
        this.model = model;
        this.year = year;
        this.capacityKg = capacityKg;
        this.capacityVolumeCbm = capacityVolumeCbm;
        this.fuelType = fuelType;
    }

    public void updateMaintenanceStatus(VehicleMaintenanceStatus status) {
        this.maintenanceStatus = status;
        if (status == VehicleMaintenanceStatus.OPERATIONAL) {
            this.lastMaintenanceDate = Instant.now();
        }
    }

    public void updateLocation(Double lat, Double lng) {
        this.currentLatitude = lat;
        this.currentLongitude = lng;
    }

    public void addOdometerKm(Double km) {
        if (km != null && km > 0) {
            this.currentOdometerKm = (this.currentOdometerKm == null ? 0.0 : this.currentOdometerKm) + km;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public Integer getYear() { return year; }
    public Double getCapacityKg() { return capacityKg; }
    public Double getCapacityVolumeCbm() { return capacityVolumeCbm; }
    public String getFuelType() { return fuelType; }
    public Double getCurrentOdometerKm() { return currentOdometerKm; }
    public VehicleMaintenanceStatus getMaintenanceStatus() { return maintenanceStatus; }
    public Instant getLastMaintenanceDate() { return lastMaintenanceDate; }
    public Instant getNextMaintenanceDueDate() { return nextMaintenanceDueDate; }
    public Double getCurrentLatitude() { return currentLatitude; }
    public Double getCurrentLongitude() { return currentLongitude; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
