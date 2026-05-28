package com.infotact.fleet.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.infotact.fleet.api.dto.DeliveryStatusUpdateRequest;
import com.infotact.fleet.api.dto.DeliveryTaskRequest;
import com.infotact.fleet.domain.DeliveryTask;
import com.infotact.fleet.exception.InvalidStateTransitionException;
import com.infotact.fleet.repository.DeliveryTaskRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DeliveryTaskServiceTest {

    @Mock
    private DeliveryTaskRepository taskRepository;

    @Test
    void createRejectsInvalidTimeWindow() {
        DeliveryTaskService service = new DeliveryTaskService(taskRepository);
        Instant start = Instant.now().plus(3, ChronoUnit.HOURS);
        Instant end = Instant.now().plus(2, ChronoUnit.HOURS);

        DeliveryTaskRequest request = new DeliveryTaskRequest(
                "Whitefield Main Road",
                "Global Logistics",
                "+919876543210",
                12.9698,
                77.7500,
                50.0,
                1.0,
                start,
                end,
                "Invalid window"
        );

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("time window start");

        verify(taskRepository, never()).save(any(DeliveryTask.class));
    }

    @Test
    void updateStatusRejectsInvalidStateJump() {
        DeliveryTaskService service = new DeliveryTaskService(taskRepository);
        DeliveryTask task = new DeliveryTask("MG Road", "Customer", "+919876543210",
                12.9757, 77.6063, 10.0, 0.2, null, null, null);
        ReflectionTestUtils.setField(task, "id", 15L);
        when(taskRepository.findById(15L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> service.updateStatus(15L, new DeliveryStatusUpdateRequest("DELIVERED")))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Invalid state transition");

        verify(taskRepository, never()).save(task);
    }
}
