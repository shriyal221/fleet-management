package com.infotact.fleet.service.optimization;

import com.infotact.fleet.domain.DeliveryTask;
import com.infotact.fleet.domain.Vehicle;
import java.util.List;

public interface RouteOptimizationStrategy {
    int[] optimize(double[][] distanceMatrix, List<DeliveryTask> tasks, Vehicle vehicle, double depotLat, double depotLng);
}
