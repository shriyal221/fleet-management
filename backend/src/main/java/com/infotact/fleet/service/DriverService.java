package com.infotact.fleet.service;

import com.infotact.fleet.api.dto.DriverRequest;
import com.infotact.fleet.api.dto.DriverResponse;
import com.infotact.fleet.domain.Driver;
import com.infotact.fleet.domain.DriverStatus;
import com.infotact.fleet.domain.Vehicle;
import com.infotact.fleet.domain.VehicleMaintenanceStatus;
import com.infotact.fleet.exception.ResourceNotFoundException;
import com.infotact.fleet.repository.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class DriverService {

    private final DriverRepository driverRepository;
    private final VehicleService vehicleService;

    public DriverService(DriverRepository driverRepository, VehicleService vehicleService) {
        this.driverRepository = driverRepository;
        this.vehicleService = vehicleService;
    }

    public List<DriverResponse> listAll() {
        return driverRepository.findAll().stream().map(this::toResponse).toList();
    }

    public DriverResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public DriverResponse create(DriverRequest request) {
        validateDriverRequest(request);
        String licenseNumber = normalize(request.licenseNumber());
        if (driverRepository.existsByLicenseNumber(licenseNumber)) {
            throw new IllegalArgumentException("Driver with license number already exists: " + licenseNumber);
        }

        Driver driver = new Driver(
                normalize(request.name()),
                optionalText(request.contactNumber()),
                optionalText(request.email()),
                licenseNumber,
                request.licenseExpiry(),
                request.shiftStart(),
                request.shiftEnd()
        );

        return toResponse(driverRepository.save(driver));
    }

    @Transactional
    public DriverResponse update(Long id, DriverRequest request) {
        Driver driver = findOrThrow(id);
        validateDriverRequest(request);
        String licenseNumber = normalize(request.licenseNumber());

        if (!driver.getLicenseNumber().equals(licenseNumber) && driverRepository.existsByLicenseNumber(licenseNumber)) {
            throw new IllegalArgumentException("Driver with license number already exists: " + licenseNumber);
        }

        driver.updateDetails(
                normalize(request.name()),
                optionalText(request.contactNumber()),
                optionalText(request.email()),
                licenseNumber,
                request.licenseExpiry(),
                request.shiftStart(),
                request.shiftEnd()
        );

        return toResponse(driverRepository.save(driver));
    }

    @Transactional
    public DriverResponse updateStatus(Long id, String status) {
        Driver driver = findOrThrow(id);
        try {
            DriverStatus nextStatus = DriverStatus.valueOf(status.toUpperCase());
            if (nextStatus == DriverStatus.AVAILABLE && !driver.isLicenseValid()) {
                throw new IllegalArgumentException("Driver cannot be marked AVAILABLE because the license is expired.");
            }
            driver.updateStatus(nextStatus);
            return toResponse(driverRepository.save(driver));
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Driver cannot")) {
                throw e;
            }
            throw new IllegalArgumentException("Invalid driver status: " + status);
        }
    }

    @Transactional
    public DriverResponse assignVehicle(Long driverId, Long vehicleId) {
        Driver driver = findOrThrow(driverId);
        Vehicle vehicle = vehicleService.findOrThrow(vehicleId);
        if (vehicle.getMaintenanceStatus() != VehicleMaintenanceStatus.OPERATIONAL) {
            throw new IllegalArgumentException("Only OPERATIONAL vehicles can be assigned to drivers.");
        }
        if (!driver.isLicenseValid()) {
            throw new IllegalArgumentException("Cannot assign vehicle because the driver license is expired.");
        }
        driver.assignVehicle(vehicle);
        return toResponse(driverRepository.save(driver));
    }

    @Transactional
    public DriverResponse unassignVehicle(Long driverId) {
        Driver driver = findOrThrow(driverId);
        driver.unassignVehicle();
        return toResponse(driverRepository.save(driver));
    }

    @Transactional
    public void delete(Long id) {
        Driver driver = findOrThrow(id);
        driverRepository.delete(driver);
    }

    public Driver findOrThrow(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with ID: " + id));
    }

    public DriverResponse toResponse(Driver d) {
        return new DriverResponse(
                d.getId(),
                d.getName(),
                d.getContactNumber(),
                d.getEmail(),
                d.getLicenseNumber(),
                d.getLicenseExpiry(),
                d.getShiftStart(),
                d.getShiftEnd(),
                d.getStatus().name(),
                d.getAssignedVehicle() != null ? d.getAssignedVehicle().getId() : null,
                d.getAssignedVehicle() != null ? d.getAssignedVehicle().getLicensePlate() : null,
                d.isLicenseValid(),
                d.getCreatedAt()
        );
    }

    private void validateDriverRequest(DriverRequest request) {
        if (request.licenseExpiry() != null && !request.licenseExpiry().isAfter(Instant.now())) {
            throw new IllegalArgumentException("Driver license expiry must be in the future.");
        }
        if (request.shiftStart() != null && request.shiftEnd() != null && request.shiftStart().equals(request.shiftEnd())) {
            throw new IllegalArgumentException("Shift start and shift end cannot be the same time.");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String optionalText(String value) {
        String normalized = normalize(value);
        return normalized == null || normalized.isBlank() ? null : normalized;
    }
}
