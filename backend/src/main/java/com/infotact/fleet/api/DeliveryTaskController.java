package com.infotact.fleet.api;

import com.infotact.fleet.api.dto.DeliveryStatusUpdateRequest;
import com.infotact.fleet.api.dto.DeliveryTaskRequest;
import com.infotact.fleet.api.dto.DeliveryTaskResponse;
import com.infotact.fleet.service.DeliveryTaskService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
public class DeliveryTaskController {

    private final DeliveryTaskService taskService;

    public DeliveryTaskController(DeliveryTaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<DeliveryTaskResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @org.springframework.data.web.PageableDefault(size = 50) org.springframework.data.domain.Pageable pageable) {
        return taskService.listAll(status, search, pageable);
    }

    @GetMapping("/unassigned")
    public List<DeliveryTaskResponse> listUnassigned() {
        return taskService.listUnassigned();
    }

    @GetMapping("/{id}")
    public DeliveryTaskResponse getById(@PathVariable Long id) {
        return taskService.getById(id);
    }

    @PostMapping
    public DeliveryTaskResponse create(@Valid @RequestBody DeliveryTaskRequest request) {
        return taskService.create(request);
    }

    @PutMapping("/{id}")
    public DeliveryTaskResponse update(@PathVariable Long id, @Valid @RequestBody DeliveryTaskRequest request) {
        return taskService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','DRIVER')")
    public DeliveryTaskResponse updateStatus(@PathVariable Long id, @Valid @RequestBody DeliveryStatusUpdateRequest request) {
        return taskService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        taskService.delete(id);
    }
}
