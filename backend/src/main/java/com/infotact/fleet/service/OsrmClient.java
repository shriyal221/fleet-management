package com.infotact.fleet.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class OsrmClient {

    private static final Logger log = LoggerFactory.getLogger(OsrmClient.class);
    private final WebClient webClient;

    @Value("${fleet.osrm.base-url:https://router.project-osrm.org}")
    private String baseUrl;

    @Value("${fleet.osrm.timeout-seconds:10}")
    private int timeoutSeconds;

    public OsrmClient(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Calls OSRM Table API to fetch distance matrix.
     * Coordinates format: list of double[] where each element is [longitude, latitude]
     */
    @SuppressWarnings("unchecked")
    public Map<String, double[][]> getDistanceMatrix(List<double[]> coordinates) {
        String coordsString = coordinates.stream()
                .map(c -> c[0] + "," + c[1])
                .collect(Collectors.joining(";"));

        String url = String.format("%s/table/v1/driving/%s?annotations=distance", baseUrl, coordsString);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .map(response -> {
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
                    throw new RuntimeException("distances not found in response");
                })
                .onErrorResume(e -> {
                    log.error("OSRM Table API failed. Using Haversine math fallback: {}", e.getMessage());
                    return Mono.just(getMockDistanceMatrix(coordinates));
                })
                .block();
    }

    /**
     * Calls OSRM Route API to fetch route summary.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Double> getRouteSummary(List<double[]> coordinates) {
        String coordsString = coordinates.stream()
                .map(c -> c[0] + "," + c[1])
                .collect(Collectors.joining(";"));

        String url = String.format("%s/route/v1/driving/%s?overview=false", baseUrl, coordsString);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .map(response -> {
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
                    throw new RuntimeException("routes not found in response");
                })
                .onErrorResume(e -> {
                    log.error("OSRM Route API failed. Using Haversine math fallback: {}", e.getMessage());
                    return Mono.just(calculateHaversineSummary(coordinates));
                })
                .block();
    }

    private Map<String, Double> calculateHaversineSummary(List<double[]> coordinates) {
        double totalDistanceMeters = 0.0;
        for (int i = 0; i < coordinates.size() - 1; i++) {
            totalDistanceMeters += haversine(coordinates.get(i)[1], coordinates.get(i)[0],
                                            coordinates.get(i + 1)[1], coordinates.get(i + 1)[0]) * 1000.0;
        }
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
