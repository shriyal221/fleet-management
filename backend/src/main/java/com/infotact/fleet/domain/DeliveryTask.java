package com.infotact.fleet.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "delivery_tasks")
public class DeliveryTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delivery_address", nullable = false, length = 255)
    private String deliveryAddress;

    @Column(name = "recipient_name", length = 100)
    private String recipientName;

    @Column(name = "recipient_phone", length = 30)
    private String recipientPhone;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "package_weight_kg")
    private Double packageWeightKg;

    @Column(name = "package_volume_cbm")
    private Double packageVolumeCbm;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 20)
    private DeliveryStatus deliveryStatus = DeliveryStatus.UNASSIGNED;

    @Column(name = "time_window_start")
    private Instant timeWindowStart;

    @Column(name = "time_window_end")
    private Instant timeWindowEnd;

    @Column(name = "actual_delivery_time")
    private Instant actualDeliveryTime;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "sequence_index")
    private Integer sequenceIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected DeliveryTask() {
        // Required by JPA
    }

    public DeliveryTask(String deliveryAddress, String recipientName, String recipientPhone, Double latitude, Double longitude, Double packageWeightKg, Double packageVolumeCbm, Instant timeWindowStart, Instant timeWindowEnd, String notes) {
        this.deliveryAddress = deliveryAddress;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.packageWeightKg = packageWeightKg;
        this.packageVolumeCbm = packageVolumeCbm;
        this.timeWindowStart = timeWindowStart;
        this.timeWindowEnd = timeWindowEnd;
        this.notes = notes;
        this.deliveryStatus = DeliveryStatus.UNASSIGNED;
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

    public void updateDetails(String deliveryAddress, String recipientName, String recipientPhone, Double latitude, Double longitude, Double packageWeightKg, Double packageVolumeCbm, Instant timeWindowStart, Instant timeWindowEnd, String notes) {
        this.deliveryAddress = deliveryAddress;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.packageWeightKg = packageWeightKg;
        this.packageVolumeCbm = packageVolumeCbm;
        this.timeWindowStart = timeWindowStart;
        this.timeWindowEnd = timeWindowEnd;
        this.notes = notes;
    }

    public void assignToRoute(Route route, Integer sequenceIndex) {
        this.route = route;
        this.sequenceIndex = sequenceIndex;
        // When assigned, task goes into DISPATCHED if route is ACTIVE, otherwise stays UNASSIGNED/PLANNED
    }

    public void unassignFromRoute() {
        this.route = null;
        this.sequenceIndex = null;
        this.deliveryStatus = DeliveryStatus.UNASSIGNED;
    }

    public void transitionStatus(DeliveryStatus newStatus) {
        validateTransition(this.deliveryStatus, newStatus);
        this.deliveryStatus = newStatus;
        if (newStatus == DeliveryStatus.DELIVERED) {
            this.actualDeliveryTime = Instant.now();
        }
    }

    private void validateTransition(DeliveryStatus current, DeliveryStatus next) {
        if (current == next) return;

        boolean valid = switch (current) {
            case UNASSIGNED -> next == DeliveryStatus.DISPATCHED;
            case DISPATCHED -> next == DeliveryStatus.IN_TRANSIT || next == DeliveryStatus.UNASSIGNED;
            case IN_TRANSIT -> next == DeliveryStatus.DELIVERED || next == DeliveryStatus.FAILED;
            case DELIVERED, FAILED -> false; // Terminal states
        };

        if (!valid) {
            throw new IllegalStateException("Invalid state transition from " + current + " to " + next);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getRecipientName() { return recipientName; }
    public String getRecipientPhone() { return recipientPhone; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Double getPackageWeightKg() { return packageWeightKg; }
    public Double getPackageVolumeCbm() { return packageVolumeCbm; }
    public DeliveryStatus getDeliveryStatus() { return deliveryStatus; }
    public Instant getTimeWindowStart() { return timeWindowStart; }
    public Instant getTimeWindowEnd() { return timeWindowEnd; }
    public Instant getActualDeliveryTime() { return actualDeliveryTime; }
    public String getNotes() { return notes; }
    public Integer getSequenceIndex() { return sequenceIndex; }
    public Route getRoute() { return route; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
