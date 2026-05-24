package com.infotact.fleet.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalTime;

@Entity
@Table(name = "drivers", uniqueConstraints = {
    @UniqueConstraint(columnNames = "license_number")
})
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "contact_number", length = 30)
    private String contactNumber;

    @Column(length = 100)
    private String email;

    @Column(name = "license_number", nullable = false, length = 50)
    private String licenseNumber;

    @Column(name = "license_expiry", nullable = false)
    private Instant licenseExpiry;

    @Column(name = "shift_start")
    private LocalTime shiftStart;

    @Column(name = "shift_end")
    private LocalTime shiftEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DriverStatus status = DriverStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_vehicle_id")
    private Vehicle assignedVehicle;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Driver() {
        // Required by JPA
    }

    public Driver(String name, String contactNumber, String email, String licenseNumber, Instant licenseExpiry, LocalTime shiftStart, LocalTime shiftEnd) {
        this.name = name;
        this.contactNumber = contactNumber;
        this.email = email;
        this.licenseNumber = licenseNumber;
        this.licenseExpiry = licenseExpiry;
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
        this.status = DriverStatus.AVAILABLE;
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

    public void updateDetails(String name, String contactNumber, String email, String licenseNumber, Instant licenseExpiry, LocalTime shiftStart, LocalTime shiftEnd) {
        this.name = name;
        this.contactNumber = contactNumber;
        this.email = email;
        this.licenseNumber = licenseNumber;
        this.licenseExpiry = licenseExpiry;
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
    }

    public void assignVehicle(Vehicle vehicle) {
        this.assignedVehicle = vehicle;
    }

    public void unassignVehicle() {
        this.assignedVehicle = null;
    }

    public void updateStatus(DriverStatus status) {
        this.status = status;
    }

    public boolean isLicenseValid() {
        return licenseExpiry != null && licenseExpiry.isAfter(Instant.now());
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getContactNumber() { return contactNumber; }
    public String getEmail() { return email; }
    public String getLicenseNumber() { return licenseNumber; }
    public Instant getLicenseExpiry() { return licenseExpiry; }
    public LocalTime getShiftStart() { return shiftStart; }
    public LocalTime getShiftEnd() { return shiftEnd; }
    public DriverStatus getStatus() { return status; }
    public Vehicle getAssignedVehicle() { return assignedVehicle; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
