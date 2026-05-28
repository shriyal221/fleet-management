package com.infotact.fleet.api;

import com.infotact.fleet.api.dto.FleetDashboardResponse;
import com.infotact.fleet.api.dto.RouteOptimizationRequest;
import com.infotact.fleet.api.dto.RouteResponse;
import com.infotact.fleet.service.RouteOptimizationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/routes")
@PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
public class RouteController {

    private final RouteOptimizationService routeService;

    public RouteController(RouteOptimizationService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public List<RouteResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @org.springframework.data.web.PageableDefault(size = 50) org.springframework.data.domain.Pageable pageable) {
        return routeService.listAll(status, search, pageable);
    }

    @GetMapping("/{id}")
    public RouteResponse getById(@PathVariable Long id) {
        return routeService.getById(id);
    }

    @PostMapping("/optimize")
    public RouteResponse optimize(@Valid @RequestBody RouteOptimizationRequest request) {
        return routeService.optimizeRoute(request);
    }

    @PostMapping("/{id}/dispatch")
    public RouteResponse dispatch(@PathVariable Long id) {
        return routeService.dispatchRoute(id);
    }

    @PostMapping("/{id}/complete")
    public RouteResponse complete(@PathVariable Long id) {
        return routeService.completeRoute(id);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','DRIVER')")
    public FleetDashboardResponse dashboard() {
        return routeService.getDashboard();
    }
}
