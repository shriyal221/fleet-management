package com.infotact.fleet.service;

import com.infotact.fleet.api.dto.VehicleRequest;
import com.infotact.fleet.api.dto.VehicleResponse;
import com.infotact.fleet.domain.Vehicle;
import com.infotact.fleet.domain.VehicleMaintenanceStatus;
import com.infotact.fleet.exception.ResourceNotFoundException;
import com.infotact.fleet.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Year;
import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public List<VehicleResponse> listAll() {
        return vehicleRepository.findAll().stream().map(this::toResponse).toList();
    }

    public VehicleResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public VehicleResponse create(VehicleRequest request) {
        validateVehicleRequest(request);
        String licensePlate = normalize(request.licensePlate());
        if (vehicleRepository.existsByLicensePlate(licensePlate)) {
            throw new IllegalArgumentException("License plate already registered: " + licensePlate);
        }

        Vehicle vehicle = new Vehicle(
                licensePlate,
                normalize(request.make()),
                normalize(request.model()),
                request.year(),
                request.capacityKg(),
                request.capacityVolumeCbm(),
                normalize(request.fuelType()).toUpperCase()
        );

        // Initial seed GPS position: Bengaluru
        vehicle.updateLocation(12.9716, 77.5946);

        return toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public VehicleResponse update(Long id, VehicleRequest request) {
        Vehicle vehicle = findOrThrow(id);
        validateVehicleRequest(request);
        String licensePlate = normalize(request.licensePlate());
        
        if (!vehicle.getLicensePlate().equals(licensePlate) && vehicleRepository.existsByLicensePlate(licensePlate)) {
            throw new IllegalArgumentException("License plate already registered: " + licensePlate);
        }

        vehicle.setLicensePlate(licensePlate);
        vehicle.updateDetails(
                normalize(request.make()),
                normalize(request.model()),
                request.year(),
                request.capacityKg(),
                request.capacityVolumeCbm(),
                normalize(request.fuelType()).toUpperCase()
        );

        return toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public VehicleResponse updateStatus(Long id, String status) {
        Vehicle vehicle = findOrThrow(id);
        try {
            VehicleMaintenanceStatus nextStatus = VehicleMaintenanceStatus.valueOf(status.toUpperCase());
            vehicle.updateMaintenanceStatus(nextStatus);
            return toResponse(vehicleRepository.save(vehicle));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid maintenance status: " + status);
        }
    }

    @Transactional
    public VehicleResponse updateLocation(Long id, Double lat, Double lng) {
        Vehicle vehicle = findOrThrow(id);
        vehicle.updateLocation(lat, lng);
        return toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public void delete(Long id) {
        Vehicle vehicle = findOrThrow(id);
        vehicleRepository.delete(vehicle);
    }

    public Vehicle findOrThrow(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));
    }

    public VehicleResponse toResponse(Vehicle v) {
        return new VehicleResponse(
                v.getId(),
                v.getLicensePlate(),
                v.getMake(),
                v.getModel(),
                v.getYear(),
                v.getCapacityKg(),
                v.getCapacityVolumeCbm(),
                v.getFuelType(),
                v.getCurrentOdometerKm(),
                v.getMaintenanceStatus().name(),
                v.getLastMaintenanceDate(),
                v.getNextMaintenanceDueDate(),
                v.getCurrentLatitude(),
                v.getCurrentLongitude(),
                v.getCreatedAt()
        );
    }

    private void validateVehicleRequest(VehicleRequest request) {
        int nextModelYear = Year.now().getValue() + 1;
        if (request.year() != null && request.year() > nextModelYear) {
            throw new IllegalArgumentException("Vehicle year cannot be later than " + nextModelYear + ".");
        }
        if (request.capacityVolumeCbm() != null && request.capacityVolumeCbm() < 0) {
            throw new IllegalArgumentException("Volume capacity cannot be negative.");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
