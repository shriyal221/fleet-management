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
import java.util.List;

@Service
public class DeliveryTaskService {

    private final DeliveryTaskRepository taskRepository;

    public DeliveryTaskService(DeliveryTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<DeliveryTaskResponse> listAll() {
        return taskRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<DeliveryTaskResponse> listUnassigned() {
        return taskRepository.findAllByRouteIsNull().stream().map(this::toResponse).toList();
    }

    public DeliveryTaskResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public DeliveryTaskResponse create(DeliveryTaskRequest request) {
        DeliveryTask task = new DeliveryTask(
                request.deliveryAddress(),
                request.recipientName(),
                request.recipientPhone(),
                request.latitude(),
                request.longitude(),
                request.packageWeightKg(),
                request.packageVolumeCbm(),
                request.timeWindowStart(),
                request.timeWindowEnd(),
                request.notes()
        );

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public DeliveryTaskResponse update(Long id, DeliveryTaskRequest request) {
        DeliveryTask task = findOrThrow(id);
        task.updateDetails(
                request.deliveryAddress(),
                request.recipientName(),
                request.recipientPhone(),
                request.latitude(),
                request.longitude(),
                request.packageWeightKg(),
                request.packageVolumeCbm(),
                request.timeWindowStart(),
                request.timeWindowEnd(),
                request.notes()
        );

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public DeliveryTaskResponse updateStatus(Long id, DeliveryStatusUpdateRequest request) {
        DeliveryTask task = findOrThrow(id);
        try {
            DeliveryStatus nextStatus = DeliveryStatus.valueOf(request.status().toUpperCase());
            task.transitionStatus(nextStatus);
            return toResponse(taskRepository.save(task));
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
}
