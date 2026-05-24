package com.infotact.fleet.service;

import com.infotact.fleet.api.dto.DriverRequest;
import com.infotact.fleet.api.dto.DriverResponse;
import com.infotact.fleet.domain.Driver;
import com.infotact.fleet.domain.DriverStatus;
import com.infotact.fleet.domain.Vehicle;
import com.infotact.fleet.exception.ResourceNotFoundException;
import com.infotact.fleet.repository.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        if (driverRepository.existsByLicenseNumber(request.licenseNumber())) {
            throw new IllegalArgumentException("Driver with license number already exists: " + request.licenseNumber());
        }

        Driver driver = new Driver(
                request.name(),
                request.contactNumber(),
                request.email(),
                request.licenseNumber(),
                request.licenseExpiry(),
                request.shiftStart(),
                request.shiftEnd()
        );

        return toResponse(driverRepository.save(driver));
    }

    @Transactional
    public DriverResponse update(Long id, DriverRequest request) {
        Driver driver = findOrThrow(id);

        if (!driver.getLicenseNumber().equals(request.licenseNumber()) && driverRepository.existsByLicenseNumber(request.licenseNumber())) {
            throw new IllegalArgumentException("Driver with license number already exists: " + request.licenseNumber());
        }

        driver.updateDetails(
                request.name(),
                request.contactNumber(),
                request.email(),
                request.licenseNumber(),
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
            driver.updateStatus(nextStatus);
            return toResponse(driverRepository.save(driver));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid driver status: " + status);
        }
    }

    @Transactional
    public DriverResponse assignVehicle(Long driverId, Long vehicleId) {
        Driver driver = findOrThrow(driverId);
        Vehicle vehicle = vehicleService.findOrThrow(vehicleId);
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
}
