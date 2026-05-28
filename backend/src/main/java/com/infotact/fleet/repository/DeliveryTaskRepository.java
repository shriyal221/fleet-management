package com.infotact.fleet.repository;

import com.infotact.fleet.domain.DeliveryStatus;
import com.infotact.fleet.domain.DeliveryTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeliveryTaskRepository extends JpaRepository<DeliveryTask, Long> {
    List<DeliveryTask> findAllByDeliveryStatus(DeliveryStatus status);
    List<DeliveryTask> findAllByRouteId(Long routeId);
    List<DeliveryTask> findAllByRouteIsNull();

    @Query("SELECT t FROM DeliveryTask t WHERE (:status IS NULL OR t.deliveryStatus = :status) " +
           "AND (:search IS NULL OR LOWER(t.deliveryAddress) LIKE :search " +
           "OR LOWER(t.recipientName) LIKE :search " +
           "OR LOWER(t.notes) LIKE :search)")
    Page<DeliveryTask> searchTasks(@Param("status") DeliveryStatus status, @Param("search") String search, Pageable pageable);
}
