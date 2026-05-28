package com.infotact.fleet.api;

import com.infotact.fleet.api.dto.DriverRequest;
import com.infotact.fleet.api.dto.DriverResponse;
import com.infotact.fleet.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drivers")
@PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping
    public List<DriverResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @org.springframework.data.web.PageableDefault(size = 50) org.springframework.data.domain.Pageable pageable) {
        return driverService.listAll(status, search, pageable);
    }

    @GetMapping("/{id}")
    public DriverResponse getById(@PathVariable Long id) {
        return driverService.getById(id);
    }

    @PostMapping
    public DriverResponse create(@Valid @RequestBody DriverRequest request) {
        return driverService.create(request);
    }

    @PutMapping("/{id}")
    public DriverResponse update(@PathVariable Long id, @Valid @RequestBody DriverRequest request) {
        return driverService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public DriverResponse updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status value is required");
        }
        return driverService.updateStatus(id, status);
    }

    @PatchMapping("/{id}/assign")
    public DriverResponse assignVehicle(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long vehicleId = body.get("vehicleId");
        if (vehicleId == null) {
            throw new IllegalArgumentException("Vehicle ID is required");
        }
        return driverService.assignVehicle(id, vehicleId);
    }

    @PatchMapping("/{id}/unassign")
    public DriverResponse unassignVehicle(@PathVariable Long id) {
        return driverService.unassignVehicle(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        driverService.delete(id);
    }
}
