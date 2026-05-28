package com.infotact.fleet.service.optimization;

import com.infotact.fleet.domain.DeliveryTask;
import com.infotact.fleet.domain.Vehicle;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class NearestNeighborStrategy implements RouteOptimizationStrategy {

    @Override
    public int[] optimize(double[][] distMatrix, List<DeliveryTask> tasks, Vehicle vehicle, double depotLat, double depotLng) {
        int n = distMatrix.length;
        boolean[] visited = new boolean[n];
        int[] order = new int[n];
        order[0] = 0; // Depot is index 0
        visited[0] = true;

        for (int step = 1; step < n; step++) {
            int current = order[step - 1];
            int nearest = -1;
            double nearestDist = Double.MAX_VALUE;

            for (int j = 0; j < n; j++) {
                if (!visited[j] && distMatrix[current][j] < nearestDist) {
                    nearestDist = distMatrix[current][j];
                    nearest = j;
                }
            }

            if (nearest == -1) {
                // Fallback: pick any unvisited
                for (int j = 0; j < n; j++) {
                    if (!visited[j]) {
                        nearest = j;
                        break;
                    }
                }
            }

            order[step] = nearest;
            visited[nearest] = true;
        }

        return order;
    }
}
