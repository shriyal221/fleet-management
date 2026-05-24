package com.infotact.fleet.service;

import com.infotact.fleet.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OsrmClient {

    private final RestTemplate restTemplate;

    @Value("${fleet.osrm.base-url:https://router.project-osrm.org}")
    private String baseUrl;

    public OsrmClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Calls OSRM Table API to fetch distance matrix.
     * Coordinates format: list of double[] where each elements is [longitude, latitude]
     */
    public Map<String, double[][]> getDistanceMatrix(List<double[]> coordinates) {
        String coordsString = coordinates.stream()
                .map(c -> c[0] + "," + c[1])
                .collect(Collectors.joining(";"));

        String url = String.format("%s/table/v1/driving/%s?annotations=distance", baseUrl, coordsString);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("distances")) {
                List<List<Double>> distancesList = (List<List<Double>>) response.get("distances");
                int size = distancesList.size();
                double[][] distances = new double[size][size];
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        Double val = distancesList.get(i).get(j);
                        distances[i][j] = val != null ? val : 0.0;
                    }
                }
                Map<String, double[][]> result = new HashMap<>();
                result.put("distances", distances);
                return result;
            }
        } catch (Exception e) {
            System.err.println("OSRM Table API failed. Using Haversine math fallback: " + e.getMessage());
        }

        // Fallback: Haversine distance matrix
        return getMockDistanceMatrix(coordinates);
    }

    /**
     * Calls OSRM Route API to fetch route summary.
     */
    public Map<String, Double> getRouteSummary(List<double[]> coordinates) {
        String coordsString = coordinates.stream()
                .map(c -> c[0] + "," + c[1])
                .collect(Collectors.joining(";"));

        String url = String.format("%s/route/v1/driving/%s?overview=false", baseUrl, coordsString);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("routes")) {
                List<Map<String, Object>> routesList = (List<Map<String, Object>>) response.get("routes");
                if (!routesList.isEmpty()) {
                    Map<String, Object> route = routesList.get(0);
                    Double distance = ((Number) route.get("distance")).doubleValue();
                    Double duration = ((Number) route.get("duration")).doubleValue();
                    Map<String, Double> summary = new HashMap<>();
                    summary.put("distance", distance);
                    summary.put("duration", duration);
                    return summary;
                }
            }
        } catch (Exception e) {
            System.err.println("OSRM Route API failed. Using Haversine math fallback: " + e.getMessage());
        }

        // Fallback: simple summary based on Haversine distance sum
        double totalDistanceMeters = 0.0;
        for (int i = 0; i < coordinates.size() - 1; i++) {
            totalDistanceMeters += haversine(coordinates.get(i)[1], coordinates.get(i)[0],
                                            coordinates.get(i + 1)[1], coordinates.get(i + 1)[0]) * 1000.0;
        }
        // Add return trip to depot to make it a loop
        totalDistanceMeters += haversine(coordinates.get(coordinates.size() - 1)[1], coordinates.get(coordinates.size() - 1)[0],
                                         coordinates.get(0)[1], coordinates.get(0)[0]) * 1000.0;

        double averageSpeedMeterPerSec = 11.11; // ~40 km/h
        double durationSeconds = totalDistanceMeters / averageSpeedMeterPerSec;

        Map<String, Double> summary = new HashMap<>();
        summary.put("distance", totalDistanceMeters);
        summary.put("duration", durationSeconds);
        return summary;
    }

    private Map<String, double[][]> getMockDistanceMatrix(List<double[]> coordinates) {
        int n = coordinates.size();
        double[][] distances = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    distances[i][j] = 0.0;
                } else {
                    // Haversine distance in meters
                    distances[i][j] = haversine(coordinates.get(i)[1], coordinates.get(i)[0],
                                                coordinates.get(j)[1], coordinates.get(j)[0]) * 1000.0;
                }
            }
        }
        Map<String, double[][]> result = new HashMap<>();
        result.put("distances", distances);
        return result;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
