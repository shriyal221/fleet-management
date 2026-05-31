package com.infotact.fleet.service.optimization;

import com.infotact.fleet.domain.DeliveryTask;
import com.infotact.fleet.domain.Vehicle;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

@Component
public class TwoOptStrategy implements RouteOptimizationStrategy {

    private final NearestNeighborStrategy nearestNeighborStrategy;

    public TwoOptStrategy(NearestNeighborStrategy nearestNeighborStrategy) {
        this.nearestNeighborStrategy = nearestNeighborStrategy;
    }

    @Override
    public int[] optimize(double[][] distMatrix, List<DeliveryTask> tasks, Vehicle vehicle, double depotLat, double depotLng) {
        // First get an initial route order from Nearest Neighbor Strategy
        int[] order = nearestNeighborStrategy.optimize(distMatrix, tasks, vehicle, depotLat, depotLng);
        int n = order.length;
        if (n <= 3) {
            return order;
        }

        int[] improved = Arrays.copyOf(order, n);
        boolean madeImprovement = true;

        while (madeImprovement) {
            madeImprovement = false;
            for (int i = 1; i < n - 1; i++) {
                for (int j = i + 1; j < n; j++) {
                    double currentDist = distMatrix[improved[i - 1]][improved[i]];
                    double newDist = distMatrix[improved[i - 1]][improved[j]];

                    if (j + 1 < n) {
                        currentDist += distMatrix[improved[j]][improved[j + 1]];
                        newDist += distMatrix[improved[i]][improved[j + 1]];
                    }

                    if (newDist < currentDist - 0.01) {
                        reverse(improved, i, j);
                        madeImprovement = true;
                    }
                }
            }
        }

        return improved;
    }

    private void reverse(int[] arr, int from, int to) {
        while (from < to) {
            int temp = arr[from];
            arr[from] = arr[to];
            arr[to] = temp;
            from++;
            to--;
        }
    }
}
