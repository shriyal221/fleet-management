package com.infotact.fleet.service.optimization;

import com.infotact.fleet.domain.DeliveryTask;
import com.infotact.fleet.domain.Route;
import com.infotact.fleet.domain.Vehicle;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class RouteScoringSystem {

    /**
     * Calculates route score from 0 (poor) to 100 (excellent).
     * Combines distance efficiency, fuel capacity buffer, and delivery window margins.
     */
    public double calculateScore(Route route, List<DeliveryTask> tasks, double penaltyFactor) {
        if (route == null) return 0.0;

        double distanceScore = calculateDistanceScore(route.getTotalDistanceKm());
        double fuelScore = calculateFuelScore(route.getTotalFuelEstimateLiters(), route.getVehicle());
        double timeWindowScore = calculateTimeWindowScore(tasks);

        // Weighted sum: 40% Distance, 30% Fuel, 30% Time Windows
        double baseScore = (distanceScore * 0.40) + (fuelScore * 0.30) + (timeWindowScore * 0.30);

        // Apply traffic penalty factor (e.g. penaltyFactor between 0.0 and 1.0)
        double finalScore = baseScore * (1.0 - (penaltyFactor * 0.15));

        return Math.max(0.0, Math.min(100.0, Math.round(finalScore * 10.0) / 10.0));
    }

    private double calculateDistanceScore(Double distanceKm) {
        if (distanceKm == null || distanceKm <= 0.0) return 0.0;
        // Optimal route distance is shorter; benchmark is 100km.
        if (distanceKm < 20.0) return 100.0;
        if (distanceKm > 250.0) return 30.0;
        return 100.0 - ((distanceKm - 20.0) / 2.3);
    }

    private double calculateFuelScore(Double fuelLiters, Vehicle vehicle) {
        if (fuelLiters == null || vehicle == null) return 50.0;
        // High fuel usage relative to capacity is penalized.
        double capacityBenchmark = vehicle.getFuelType().equalsIgnoreCase("ELECTRIC") ? 100.0 : 60.0; // Liters / kWh
        double usageRatio = fuelLiters / capacityBenchmark;
        if (usageRatio <= 0.2) return 100.0;
        if (usageRatio >= 1.0) return 10.0;
        return 100.0 - (usageRatio - 0.2) * 112.5;
    }

    private double calculateTimeWindowScore(List<DeliveryTask> tasks) {
        if (tasks == null || tasks.isEmpty()) return 100.0;
        // Check if there are close calls on time windows.
        long violatedCount = tasks.stream()
                .filter(t -> t.getActualDeliveryTime() != null && t.getTimeWindowEnd() != null
                        && t.getActualDeliveryTime().isAfter(t.getTimeWindowEnd()))
                .count();

        if (violatedCount > 0) {
            return Math.max(0.0, 100.0 - (violatedCount * 30.0));
        }
        return 100.0;
    }
}
