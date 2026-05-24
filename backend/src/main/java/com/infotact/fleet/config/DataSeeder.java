package com.infotact.fleet.config;

import com.infotact.fleet.domain.*;
import com.infotact.fleet.repository.*;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(
        AppUserRepository userRepository,
        VehicleRepository vehicleRepository,
        DriverRepository driverRepository,
        DeliveryTaskRepository deliveryTaskRepository,
        PasswordEncoder passwordEncoder,
        @Value("${seed.admin-password:admin123}") String adminPassword,
        @Value("${seed.dispatcher-password:dispatcher123}") String dispatcherPassword,
        @Value("${seed.operator-password:operator123}") String operatorPassword
    ) {
        return args -> {
            // Seed Admin User
            if (!userRepository.existsByUsername("admin")) {
                userRepository.save(new AppUser("admin", passwordEncoder.encode(adminPassword), Role.ADMIN, "System Administrator", "admin@infotact.local", "+91 90000 00001"));
            }

            // Seed Dispatcher User
            if (!userRepository.existsByUsername("dispatcher")) {
                userRepository.save(new AppUser("dispatcher", passwordEncoder.encode(dispatcherPassword), Role.DISPATCHER, "Fleet Dispatcher", "dispatcher@infotact.local", "+91 90000 00010"));
            }

            // Seed Vehicles
            if (!vehicleRepository.existsByLicensePlate("KA-01-AB-1234")) {
                Vehicle v1 = new Vehicle("KA-01-AB-1234", "Tata", "Ace Gold", 2024, 750.0, 4.5, "DIESEL");
                v1.updateLocation(12.9716, 77.5946);
                vehicleRepository.save(v1);
            }
            if (!vehicleRepository.existsByLicensePlate("KA-01-CD-5678")) {
                Vehicle v2 = new Vehicle("KA-01-CD-5678", "Mahindra", "Bolero Pickup", 2023, 1250.0, 6.0, "DIESEL");
                v2.updateLocation(12.9352, 77.6245);
                vehicleRepository.save(v2);
            }
            if (!vehicleRepository.existsByLicensePlate("KA-02-EF-9012")) {
                Vehicle v3 = new Vehicle("KA-02-EF-9012", "Ashok Leyland", "Dost+", 2024, 2000.0, 10.0, "DIESEL");
                v3.updateLocation(12.9783, 77.5712);
                vehicleRepository.save(v3);
            }
            if (!vehicleRepository.existsByLicensePlate("KA-03-GH-3456")) {
                Vehicle v4 = new Vehicle("KA-03-GH-3456", "Tata", "Intra V30", 2022, 1500.0, 7.5, "CNG");
                v4.updateMaintenanceStatus(VehicleMaintenanceStatus.SCHEDULED_MAINTENANCE);
                vehicleRepository.save(v4);
            }
            if (!vehicleRepository.existsByLicensePlate("KA-04-IJ-7890")) {
                Vehicle v5 = new Vehicle("KA-04-IJ-7890", "EV Motors", "Elecstar E200", 2025, 500.0, 3.0, "ELECTRIC");
                v5.updateLocation(12.9250, 77.5897);
                vehicleRepository.save(v5);
            }

            // Seed Drivers
            if (!driverRepository.existsByLicenseNumber("KA-DL-2020-00101")) {
                Driver d1 = new Driver("Ramesh Kumar", "+91 98765 43210", "ramesh@infotact.local",
                        "KA-DL-2020-00101", Instant.now().plus(365, ChronoUnit.DAYS),
                        LocalTime.of(8, 0), LocalTime.of(18, 0));
                if (vehicleRepository.existsByLicensePlate("KA-01-AB-1234")) {
                    d1.assignVehicle(vehicleRepository.findByLicensePlate("KA-01-AB-1234").orElseThrow());
                }
                driverRepository.save(d1);
            }
            if (!driverRepository.existsByLicenseNumber("KA-DL-2021-00202")) {
                Driver d2 = new Driver("Suresh Babu", "+91 98765 43211", "suresh@infotact.local",
                        "KA-DL-2021-00202", Instant.now().plus(200, ChronoUnit.DAYS),
                        LocalTime.of(9, 0), LocalTime.of(19, 0));
                if (vehicleRepository.existsByLicensePlate("KA-01-CD-5678")) {
                    d2.assignVehicle(vehicleRepository.findByLicensePlate("KA-01-CD-5678").orElseThrow());
                }
                driverRepository.save(d2);
            }
            if (!driverRepository.existsByLicenseNumber("KA-DL-2019-00303")) {
                driverRepository.save(new Driver("Venkatesh R", "+91 98765 43212", "venkatesh@infotact.local",
                        "KA-DL-2019-00303", Instant.now().plus(500, ChronoUnit.DAYS),
                        LocalTime.of(6, 0), LocalTime.of(14, 0)));
            }
            if (!driverRepository.existsByLicenseNumber("KA-DL-2022-00404")) {
                driverRepository.save(new Driver("Anjali Sharma", "+91 98765 43213", "anjali@infotact.local",
                        "KA-DL-2022-00404", Instant.now().plus(700, ChronoUnit.DAYS),
                        LocalTime.of(10, 0), LocalTime.of(20, 0)));
            }

            // Seed Delivery Tasks (Bengaluru coordinates)
            if (deliveryTaskRepository.count() == 0) {
                deliveryTaskRepository.save(new DeliveryTask(
                        "100 Feet Road, Indiranagar", "Priya Electronics", "+91 80 4567 0001",
                        12.9784, 77.6408, 25.0, 0.5,
                        Instant.now().plus(2, ChronoUnit.HOURS), Instant.now().plus(4, ChronoUnit.HOURS),
                        "Handle with care - electronics"));
                deliveryTaskRepository.save(new DeliveryTask(
                        "MG Road, Brigade Gateway", "Fashion Hub Store", "+91 80 4567 0002",
                        12.9757, 77.6063, 15.0, 0.3,
                        Instant.now().plus(1, ChronoUnit.HOURS), Instant.now().plus(3, ChronoUnit.HOURS),
                        "Fragile garment packaging"));
                deliveryTaskRepository.save(new DeliveryTask(
                        "Koramangala 4th Block, Sony Signal", "Tech Solutions Pvt Ltd", "+91 80 4567 0003",
                        12.9352, 77.6245, 50.0, 1.2,
                        Instant.now().plus(3, ChronoUnit.HOURS), Instant.now().plus(6, ChronoUnit.HOURS),
                        "Server rack components"));
                deliveryTaskRepository.save(new DeliveryTask(
                        "Jayanagar 4th Block, Cool Joint", "Sri Lakshmi Enterprises", "+91 80 4567 0004",
                        12.9254, 77.5834, 10.0, 0.2,
                        Instant.now().plus(2, ChronoUnit.HOURS), Instant.now().plus(5, ChronoUnit.HOURS),
                        "Office supplies delivery"));
                deliveryTaskRepository.save(new DeliveryTask(
                        "Whitefield Main Road, ITPL", "Global Logistics Inc", "+91 80 4567 0005",
                        12.9698, 77.7500, 100.0, 3.0,
                        Instant.now().plus(4, ChronoUnit.HOURS), Instant.now().plus(8, ChronoUnit.HOURS),
                        "Bulk warehouse supplies"));
                deliveryTaskRepository.save(new DeliveryTask(
                        "HSR Layout, Sector 7", "Home Essentials", "+91 80 4567 0006",
                        12.9121, 77.6446, 8.0, 0.15,
                        Instant.now().plus(1, ChronoUnit.HOURS), Instant.now().plus(3, ChronoUnit.HOURS),
                        "Small parcel - kitchen appliances"));
                deliveryTaskRepository.save(new DeliveryTask(
                        "Electronic City Phase 1, Infosys Gate", "Chip Design Labs", "+91 80 4567 0007",
                        12.8456, 77.6603, 35.0, 0.8,
                        Instant.now().plus(5, ChronoUnit.HOURS), Instant.now().plus(9, ChronoUnit.HOURS),
                        "Testing equipment"));
                deliveryTaskRepository.save(new DeliveryTask(
                        "Malleshwaram, 8th Cross", "Bookworm Library", "+91 80 4567 0008",
                        12.9965, 77.5708, 20.0, 0.4,
                        Instant.now().plus(2, ChronoUnit.HOURS), Instant.now().plus(4, ChronoUnit.HOURS),
                        "Book shipment - 4 cartons"));
            }
        };
    }
}
