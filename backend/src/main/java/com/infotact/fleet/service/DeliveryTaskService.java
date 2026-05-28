package com.infotact.fleet.service;

import com.infotact.fleet.api.dto.DeliveryStatusUpdateRequest;
import com.infotact.fleet.api.dto.DeliveryTaskRequest;
import com.infotact.fleet.api.dto.DeliveryTaskResponse;
import com.infotact.fleet.domain.DeliveryStatus;
import com.infotact.fleet.domain.DeliveryTask;
import com.infotact.fleet.exception.InvalidStateTransitionException;
import com.infotact.fleet.exception.ResourceNotFoundException;
import com.infotact.fleet.repository.DeliveryTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
@SuppressWarnings("null")
public class DeliveryTaskService {

    private final DeliveryTaskRepository taskRepository;
    private final AuditService auditService;

    public DeliveryTaskService(DeliveryTaskRepository taskRepository, AuditService auditService) {
        this.taskRepository = taskRepository;
        this.auditService = auditService;
    }

    public List<DeliveryTaskResponse> listAll() {
        return taskRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DeliveryTaskResponse> listAll(String status, String search, org.springframework.data.domain.Pageable pageable) {
        DeliveryStatus deliveryStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                deliveryStatus = DeliveryStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore
            }
        }
        String searchQuery = (search != null && !search.isBlank()) ? "%" + search.trim().toLowerCase() + "%" : null;
        return taskRepository.searchTasks(deliveryStatus, searchQuery, pageable)
                .map(this::toResponse)
                .getContent();
    }

    public List<DeliveryTaskResponse> listUnassigned() {
        return taskRepository.findAllByRouteIsNull().stream().map(this::toResponse).toList();
    }

    public DeliveryTaskResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public DeliveryTaskResponse create(DeliveryTaskRequest request) {
        validateDeliveryTaskRequest(request);
        DeliveryTask task = new DeliveryTask(
                normalize(request.deliveryAddress()),
                optionalText(request.recipientName()),
                optionalText(request.recipientPhone()),
                request.latitude(),
                request.longitude(),
                request.packageWeightKg(),
                request.packageVolumeCbm(),
                request.timeWindowStart(),
                request.timeWindowEnd(),
                request.notes()
        );

        DeliveryTask saved = taskRepository.save(task);
        auditService.log("TASK_CREATE", "Created delivery task ID: " + saved.getId() + " for " + saved.getRecipientName() + " at " + saved.getDeliveryAddress());
        return toResponse(saved);
    }

    @Transactional
    public DeliveryTaskResponse update(Long id, DeliveryTaskRequest request) {
        DeliveryTask task = findOrThrow(id);
        validateDeliveryTaskRequest(request);
        task.updateDetails(
                normalize(request.deliveryAddress()),
                optionalText(request.recipientName()),
                optionalText(request.recipientPhone()),
                request.latitude(),
                request.longitude(),
                request.packageWeightKg(),
                request.packageVolumeCbm(),
                request.timeWindowStart(),
                request.timeWindowEnd(),
                request.notes()
        );

        DeliveryTask saved = taskRepository.save(task);
        auditService.log("TASK_UPDATE", "Updated details for delivery task ID: " + saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public DeliveryTaskResponse updateStatus(Long id, DeliveryStatusUpdateRequest request) {
        DeliveryTask task = findOrThrow(id);
        try {
            DeliveryStatus nextStatus = DeliveryStatus.valueOf(request.status().toUpperCase());
            DeliveryStatus oldStatus = task.getDeliveryStatus();
            task.transitionStatus(nextStatus);
            DeliveryTask saved = taskRepository.save(task);
            auditService.log("TASK_STATUS_UPDATE", "Updated delivery task ID: " + saved.getId() + " status from " + oldStatus + " to " + nextStatus);
            return toResponse(saved);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid delivery status value: " + request.status());
        } catch (IllegalStateException e) {
            throw new InvalidStateTransitionException(e.getMessage());
        }
    }

    @Transactional
    public void delete(Long id) {
        DeliveryTask task = findOrThrow(id);
        taskRepository.delete(task);
        auditService.log("TASK_DELETE", "Deleted delivery task ID: " + task.getId() + " at " + task.getDeliveryAddress());
    }

    public DeliveryTask findOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery task not found with ID: " + id));
    }

    public DeliveryTaskResponse toResponse(DeliveryTask t) {
        return new DeliveryTaskResponse(
                t.getId(),
                t.getDeliveryAddress(),
                t.getRecipientName(),
                t.getRecipientPhone(),
                t.getLatitude(),
                t.getLongitude(),
                t.getPackageWeightKg(),
                t.getPackageVolumeCbm(),
                t.getDeliveryStatus().name(),
                t.getTimeWindowStart(),
                t.getTimeWindowEnd(),
                t.getActualDeliveryTime(),
                t.getNotes(),
                t.getSequenceIndex(),
                t.getRoute() != null ? t.getRoute().getId() : null,
                t.getRoute() != null ? t.getRoute().getRouteName() : null,
                t.getCreatedAt()
        );
    }

    private void validateDeliveryTaskRequest(DeliveryTaskRequest request) {
        if (request.timeWindowStart() != null && request.timeWindowEnd() != null
                && !request.timeWindowStart().isBefore(request.timeWindowEnd())) {
            throw new IllegalArgumentException("Delivery time window start must be before time window end.");
        }
        if (request.timeWindowEnd() != null && request.timeWindowEnd().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Delivery time window end cannot be in the past.");
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
