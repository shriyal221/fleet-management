package com.infotact.fleet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.infotact.fleet.api.dto.DeliveryStatusUpdateRequest;
import com.infotact.fleet.api.dto.DeliveryTaskRequest;
import com.infotact.fleet.api.dto.DeliveryTaskResponse;
import com.infotact.fleet.domain.DeliveryTask;
import com.infotact.fleet.exception.InvalidStateTransitionException;
import com.infotact.fleet.repository.DeliveryTaskRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class DeliveryTaskServiceTest {

    @Mock
    private DeliveryTaskRepository taskRepository;

    @Mock
    private AuditService auditService;

    private DeliveryTaskService service;

    @BeforeEach
    void setUp() {
        service = new DeliveryTaskService(taskRepository, auditService);
    }

    @Test
    void createRejectsInvalidTimeWindow() {
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
    void createDeliveryTaskSuccessfullyWhenInputsAreValid() {
        Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
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
                "Valid window"
        );

        when(taskRepository.save(any(DeliveryTask.class))).thenAnswer(invocation -> {
            DeliveryTask task = invocation.getArgument(0);
            ReflectionTestUtils.setField(task, "id", 100L);
            return task;
        });

        DeliveryTaskResponse response = service.create(request);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.deliveryAddress()).isEqualTo("Whitefield Main Road");
        assertThat(response.deliveryStatus()).isEqualTo("UNASSIGNED");
        verify(auditService).log(anyString(), anyString());
    }

    @Test
    void updateStatusRejectsInvalidStateJump() {
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
