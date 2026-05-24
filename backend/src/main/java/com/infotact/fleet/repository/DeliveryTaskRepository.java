package com.infotact.fleet.repository;

import com.infotact.fleet.domain.DeliveryStatus;
import com.infotact.fleet.domain.DeliveryTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeliveryTaskRepository extends JpaRepository<DeliveryTask, Long> {
    List<DeliveryTask> findAllByDeliveryStatus(DeliveryStatus status);
    List<DeliveryTask> findAllByRouteId(Long routeId);
    List<DeliveryTask> findAllByRouteIsNull();
}
