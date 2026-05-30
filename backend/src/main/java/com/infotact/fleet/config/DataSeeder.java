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
        RouteRepository routeRepository,
        PasswordEncoder passwordEncoder,
        @Value("${seed.admin-password:admin123}") String adminPassword,
        @Value("${seed.dispatcher-password:dispatcher123}") String dispatcherPassword,
        @Value("${seed.operator-password:operator123}") String operatorPassword
    ) {
        return args -> {
            // Skip seeding if demo data already exists (prevents data loss on restart)
            if (userRepository.count() > 0) {
                System.out.println("Database already seeded — skipping. Clear tables manually or drop the DB to re-seed.");
                return;
            }

            // 1. Clear old data in order of foreign key relationships to prevent constraint violations
            System.out.println("Cleaning database for fresh seeding...");
            deliveryTaskRepository.deleteAll();
            routeRepository.deleteAll();
            driverRepository.deleteAll();
            vehicleRepository.deleteAll();
            userRepository.deleteAll();

            // 2. Seed Role-Based Demo Users
            System.out.println("Seeding enterprise role-based users...");
            
            // ADMIN
            AppUser admin = new AppUser(
                "admin@fleetpro.com", 
                passwordEncoder.encode(adminPassword), 
                Role.ADMIN, 
                "Pranav Sharma", 
                "admin@fleetpro.com", 
                "+91 98800 00001"
            );
            userRepository.save(admin);

            // DISPATCHER
            AppUser dispatcher = new AppUser(
                "dispatcher@fleetpro.com", 
                passwordEncoder.encode(dispatcherPassword), 
                Role.DISPATCHER, 
                "Meera Deshmukh", 
                "dispatcher@fleetpro.com", 
                "+91 98800 00010"
            );
            userRepository.save(dispatcher);

            // DRIVER
            AppUser driverUser = new AppUser(
                "driver@fleetpro.com", 
                passwordEncoder.encode(operatorPassword), 
                Role.DRIVER, 
                "Karthik Gowda", 
                "driver@fleetpro.com", 
                "+91 99000 87654"
            );
            userRepository.save(driverUser);

            // 3. Seed 5 Realistic Commercial Vehicles
            System.out.println("Seeding realistic cargo and transport vehicles...");
            
            // Vehicle 1: Cargo Truck (Diesel) - Pune Hub
            Vehicle v1 = new Vehicle("MH-12-QW-9876", "Tata", "Ultra T.7", 2023, 3500.0, 15.0, "DIESEL");
            v1.updateLocation(18.5204, 73.8567);
            v1.addOdometerKm(12450.0);
            vehicleRepository.save(v1);

            // Vehicle 2: Delivery Van (Diesel) - Mumbai Hub
            Vehicle v2 = new Vehicle("MH-02-XY-4321", "Mahindra", "Bolero Maxx", 2024, 1300.0, 6.5, "DIESEL");
            v2.updateLocation(19.0760, 72.8777);
            v2.addOdometerKm(3800.0);
            vehicleRepository.save(v2);

            // Vehicle 3: Mini Transport (CNG) - Bangalore Hub
            Vehicle v3 = new Vehicle("KA-03-MJ-5678", "Ashok Leyland", "Dost+", 2024, 1500.0, 7.5, "CNG");
            v3.updateLocation(12.9716, 77.5946);
            v3.addOdometerKm(5600.0);
            vehicleRepository.save(v3);

            // Vehicle 4: Refrigerated Truck (Electric) - Hyderabad Hub
            Vehicle v4 = new Vehicle("TS-07-UE-2468", "EV Motors", "Elecstar E300", 2025, 2500.0, 12.0, "ELECTRIC");
            v4.updateLocation(17.3850, 78.4867);
            v4.addOdometerKm(1800.0);
            vehicleRepository.save(v4);

            // Vehicle 5: Mini Transport EV (Electric) - Chennai Hub
            Vehicle v5 = new Vehicle("TN-01-DR-1357", "Tata", "Ace Gold EV", 2025, 600.0, 3.5, "ELECTRIC");
            v5.updateLocation(13.0827, 80.2707);
            v5.addOdometerKm(900.0);
            vehicleRepository.save(v5);

            // 4. Seed 5 Realistic Drivers
            System.out.println("Seeding professional drivers with active vehicle couplings...");
            
            // Driver 1: Rajesh Sekhar (Pune)
            Driver d1 = new Driver(
                "Rajesh Sekhar", 
                "+91 98800 12345", 
                "rajesh.sekhar@fleetpro.com",
                "MH-DL-2015-0098765", 
                Instant.now().plus(400, ChronoUnit.DAYS),
                LocalTime.of(8, 0), 
                LocalTime.of(18, 0)
            );
            d1.assignVehicle(v1);
            d1.updateStatus(DriverStatus.AVAILABLE);
            driverRepository.save(d1);

            // Driver 2: Arjun Sawant (Mumbai)
            Driver d2 = new Driver(
                "Arjun Sawant", 
                "+91 98200 54321", 
                "arjun.sawant@fleetpro.com",
                "MH-DL-2018-0043210", 
                Instant.now().plus(250, ChronoUnit.DAYS),
                LocalTime.of(9, 0), 
                LocalTime.of(19, 0)
            );
            d2.assignVehicle(v2);
            d2.updateStatus(DriverStatus.AVAILABLE);
            driverRepository.save(d2);

            // Driver 3: Karthik Gowda (Bangalore)
            Driver d3 = new Driver(
                "Karthik Gowda", 
                "+91 99000 87654", 
                "karthik.gowda@fleetpro.com",
                "KA-DL-2020-0056789", 
                Instant.now().plus(600, ChronoUnit.DAYS),
                LocalTime.of(6, 0), 
                LocalTime.of(16, 0)
            );
            d3.assignVehicle(v3);
            d3.updateStatus(DriverStatus.ON_ROUTE); // ACTIVE ROUTE ON STARTUP
            driverRepository.save(d3);

            // Driver 4: Mohammad Ali (Hyderabad)
            Driver d4 = new Driver(
                "Mohammad Ali", 
                "+91 97000 24680", 
                "mohammad.ali@fleetpro.com",
                "TS-DL-2019-0024680", 
                Instant.now().plus(320, ChronoUnit.DAYS),
                LocalTime.of(10, 0), 
                LocalTime.of(20, 0)
            );
            d4.assignVehicle(v4);
            d4.updateStatus(DriverStatus.ON_ROUTE); // ACTIVE ROUTE ON STARTUP
            driverRepository.save(d4);

            // Driver 5: Srinivasan R (Chennai)
            Driver d5 = new Driver(
                "Srinivasan R", 
                "+91 96000 13579", 
                "srinivasan.r@fleetpro.com",
                "TN-DL-2021-0013579", 
                Instant.now().plus(750, ChronoUnit.DAYS),
                LocalTime.of(7, 0), 
                LocalTime.of(17, 0)
            );
            d5.assignVehicle(v5);
            d5.updateStatus(DriverStatus.AVAILABLE);
            driverRepository.save(d5);

            // 5. Seed 25 Realistic Delivery Tasks (5 per major Indian Hub)
            System.out.println("Seeding realistic city outbound delivery tasks...");
            
            // --- Bangalore Tasks (HQ) ---
            DeliveryTask tBang1 = new DeliveryTask("100 Feet Road, Indiranagar", "Priya Electronics", "+91 80 4567 0001", 12.9784, 77.6408, 25.0, 0.5, Instant.now().plus(1, ChronoUnit.HOURS), Instant.now().plus(4, ChronoUnit.HOURS), "Handle with care - electronics");
            DeliveryTask tBang2 = new DeliveryTask("MG Road, Brigade Gateway", "Fashion Hub Store", "+91 80 4567 0002", 12.9757, 77.6063, 15.0, 0.3, Instant.now().plus(2, ChronoUnit.HOURS), Instant.now().plus(5, ChronoUnit.HOURS), "Fragile garment packaging");
            DeliveryTask tBang3 = new DeliveryTask("Koramangala 4th Block, Sony Signal", "Tech Solutions Pvt Ltd", "+91 80 4567 0003", 12.9352, 77.6245, 50.0, 1.2, Instant.now().plus(1, ChronoUnit.HOURS), Instant.now().plus(6, ChronoUnit.HOURS), "Server rack components");
            DeliveryTask tBang4 = new DeliveryTask("Jayanagar 4th Block, Cool Joint", "Sri Lakshmi Enterprises", "+91 80 4567 0004", 12.9254, 77.5834, 10.0, 0.2, Instant.now().plus(3, ChronoUnit.HOURS), Instant.now().plus(7, ChronoUnit.HOURS), "Office supplies delivery");
            DeliveryTask tBang5 = new DeliveryTask("HSR Layout, Sector 7", "Home Essentials", "+91 80 4567 0005", 12.9121, 77.6446, 8.0, 0.15, Instant.now().plus(2, ChronoUnit.HOURS), Instant.now().plus(5, ChronoUnit.HOURS), "Small kitchen appliances");
            
            deliveryTaskRepository.save(tBang1);
            deliveryTaskRepository.save(tBang2);
            deliveryTaskRepository.save(tBang3);
            deliveryTaskRepository.save(tBang4);
            deliveryTaskRepository.save(tBang5);

            // --- Mumbai Tasks ---
            DeliveryTask tMum1 = new DeliveryTask("Link Road, Andheri West", "Mumbai Digital Mart", "+91 22 2634 1111", 19.1197, 72.8468, 85.0, 1.8, Instant.now().minus(4, ChronoUnit.HOURS), Instant.now().minus(1, ChronoUnit.HOURS), "Consumer appliances bulk delivery");
            DeliveryTask tMum2 = new DeliveryTask("Marine Drive, Nariman Point", "State Bank Center", "+91 22 2282 2222", 18.9256, 72.8242, 12.0, 0.25, Instant.now().minus(5, ChronoUnit.HOURS), Instant.now().minus(2, ChronoUnit.HOURS), "Confidential corporate documents");
            DeliveryTask tMum3 = new DeliveryTask("LBS Marg, Ghatkopar East", "Vikas Trading Corp", "+91 22 2511 3333", 19.0863, 72.9085, 220.0, 3.4, Instant.now().minus(6, ChronoUnit.HOURS), Instant.now().minus(1, ChronoUnit.HOURS), "Industrial raw materials package");
            DeliveryTask tMum4 = new DeliveryTask("Linking Road, Bandra West", "Elite Apparels Store", "+91 22 2640 4444", 19.0583, 72.8302, 45.0, 0.9, Instant.now().plus(2, ChronoUnit.HOURS), Instant.now().plus(6, ChronoUnit.HOURS), "Designer garments cargo");
            DeliveryTask tMum5 = new DeliveryTask("SV Road, Borivali East", "Metro Food Hub", "+91 22 2801 5555", 19.2307, 72.8569, 130.0, 2.1, Instant.now().plus(4, ChronoUnit.HOURS), Instant.now().plus(8, ChronoUnit.HOURS), "Perishable fresh food supplies");

            deliveryTaskRepository.save(tMum1);
            deliveryTaskRepository.save(tMum2);
            deliveryTaskRepository.save(tMum3);
            deliveryTaskRepository.save(tMum4);
            deliveryTaskRepository.save(tMum5);

            // --- Pune Tasks ---
            DeliveryTask tPune1 = new DeliveryTask("F.C. Road, Shivajinagar", "Deccan Bookstore", "+91 20 2565 0011", 18.5244, 73.8412, 15.0, 0.4, Instant.now().plus(1, ChronoUnit.HOURS), Instant.now().plus(4, ChronoUnit.HOURS), "Educational textbook crates");
            DeliveryTask tPune2 = new DeliveryTask("Viman Nagar, Phoenix Mall", "Skechers Outlet", "+91 20 6608 0022", 18.5679, 73.9143, 60.0, 1.5, Instant.now().plus(2, ChronoUnit.HOURS), Instant.now().plus(5, ChronoUnit.HOURS), "Retail footwear pallets");
            DeliveryTask tPune3 = new DeliveryTask("Koregaon Park, Lane 7", "Nisarg Organic Foods", "+91 20 2612 0033", 18.5362, 73.8939, 40.0, 0.8, Instant.now().plus(3, ChronoUnit.HOURS), Instant.now().plus(6, ChronoUnit.HOURS), "Organic cold-pressed oils");
            DeliveryTask tPune4 = new DeliveryTask("Baner Road, Balewadi", "Cognizant Technology Park", "+91 20 6640 0044", 18.5594, 73.8052, 75.0, 1.6, Instant.now().plus(2, ChronoUnit.HOURS), Instant.now().plus(6, ChronoUnit.HOURS), "Office equipment upgrade");
            DeliveryTask tPune5 = new DeliveryTask("Hadapsar, Magarpatta City", "Noble Diagnostics", "+91 20 2689 0055", 18.5144, 73.9268, 20.0, 0.6, Instant.now().plus(1, ChronoUnit.HOURS), Instant.now().plus(4, ChronoUnit.HOURS), "Medical laboratory kits");

            deliveryTaskRepository.save(tPune1);
            deliveryTaskRepository.save(tPune2);
            deliveryTaskRepository.save(tPune3);
            deliveryTaskRepository.save(tPune4);
            deliveryTaskRepository.save(tPune5);

            // --- Hyderabad Tasks ---
            DeliveryTask tHyd1 = new DeliveryTask("HITEC City, Mindspace Layout", "PharmaCare Labs", "+91 40 4001 9876", 17.4411, 78.3826, 120.0, 2.5, Instant.now().plus(1, ChronoUnit.HOURS), Instant.now().plus(5, ChronoUnit.HOURS), "Temperature-controlled insulin shipment");
            DeliveryTask tHyd2 = new DeliveryTask("Gachibowli, DLF Cyber City", "HCL Technologies", "+91 40 4002 9877", 17.4435, 78.3489, 45.0, 1.0, Instant.now().plus(2, ChronoUnit.HOURS), Instant.now().plus(6, ChronoUnit.HOURS), "Network switchboards - fragile");
            DeliveryTask tHyd3 = new DeliveryTask("Banjara Hills, Road No 1", "Rainbow Hospitals", "+91 40 4003 9878", 17.4156, 78.4489, 30.0, 0.7, Instant.now().plus(3, ChronoUnit.HOURS), Instant.now().plus(7, ChronoUnit.HOURS), "Infant care pediatric monitors");
            DeliveryTask tHyd4 = new DeliveryTask("Jubilee Hills, Checkpost Plaza", "Suresh Productions", "+91 40 4004 9879", 17.4312, 78.4063, 15.0, 0.4, Instant.now().plus(2, ChronoUnit.HOURS), Instant.now().plus(6, ChronoUnit.HOURS), "Digital camera lenses - high value");
            DeliveryTask tHyd5 = new DeliveryTask("Secunderabad, Clock Tower", "South Central Railway HQ", "+91 40 4005 9880", 17.4447, 78.5012, 190.0, 3.2, Instant.now().plus(4, ChronoUnit.HOURS), Instant.now().plus(8, ChronoUnit.HOURS), "Heavy mechanical machinery components");

            deliveryTaskRepository.save(tHyd1);
            deliveryTaskRepository.save(tHyd2);
            deliveryTaskRepository.save(tHyd3);
            deliveryTaskRepository.save(tHyd4);
            deliveryTaskRepository.save(tHyd5);

            // --- Chennai Tasks ---
            DeliveryTask tChe1 = new DeliveryTask("Anna Salai, Teynampet", "Automotive Components India", "+91 44 2432 0101", 13.0405, 80.2467, 180.0, 2.0, Instant.now().plus(2, ChronoUnit.HOURS), Instant.now().plus(6, ChronoUnit.HOURS), "OEM engine spare parts");
            DeliveryTask tChe2 = new DeliveryTask("OMR Road, Karapakkam", "Infosys Campus", "+91 44 2433 0102", 12.9156, 80.2242, 50.0, 1.1, Instant.now().plus(3, ChronoUnit.HOURS), Instant.now().plus(7, ChronoUnit.HOURS), "Ergonomic workspace seating units");
            DeliveryTask tChe3 = new DeliveryTask("T. Nagar, Usman Road Shop", "Saravana Gold Plaza", "+91 44 2434 0103", 13.0326, 80.2351, 10.0, 0.2, Instant.now().plus(1, ChronoUnit.HOURS), Instant.now().plus(5, ChronoUnit.HOURS), "High value retail display units");
            DeliveryTask tChe4 = new DeliveryTask("Adyar, Kasturba Nagar Cross", "IIT Madras Research Park", "+91 44 2435 0104", 13.0063, 80.2526, 40.0, 0.9, Instant.now().plus(2, ChronoUnit.HOURS), Instant.now().plus(6, ChronoUnit.HOURS), "Laboratory glassware cabinets");
            DeliveryTask tChe5 = new DeliveryTask("Velachery Main Road, Grand Mall", "Decathlon Sports Hub", "+91 44 2436 0105", 12.9802, 80.2228, 95.0, 2.3, Instant.now().plus(4, ChronoUnit.HOURS), Instant.now().plus(8, ChronoUnit.HOURS), "Bulk sportswear cardboard cartons");

            deliveryTaskRepository.save(tChe1);
            deliveryTaskRepository.save(tChe2);
            deliveryTaskRepository.save(tChe3);
            deliveryTaskRepository.save(tChe4);
            deliveryTaskRepository.save(tChe5);

            // 6. Seed 5 Realistic Optimized Routes (1 per Hub, varying statuses)
            System.out.println("Seeding fully integrated logistics routes...");

            // --- Route 1: Bangalore Hub (ACTIVE ROUTE - Animating immediately!) ---
            // Depot: Bangalore Depot (12.9716, 77.5946)
            Route rBang = new Route("RT-BANGALORE-01", v3, d3, 12.9716, 77.5946);
            rBang.setOptimizationResult(
                24.5,  // Total distance in km
                72,    // Duration in minutes
                3.06,  // Fuel consumption (CNG efficiency based)
                String.format("[%d,%d,%d]", tBang3.getId(), tBang2.getId(), tBang1.getId())
            );
            rBang.setRouteScore(96.5);
            rBang.dispatch(); // Transitions status to ACTIVE, sets dispatchedAt = Instant.now()
            rBang = routeRepository.save(rBang);

            // Assign Tasks to Bangalore Route with active tracking states
            tBang3.assignToRoute(rBang, 1);
            tBang3.transitionStatus(DeliveryStatus.DISPATCHED);
            tBang3.transitionStatus(DeliveryStatus.IN_TRANSIT); // Underway
            deliveryTaskRepository.save(tBang3);

            tBang2.assignToRoute(rBang, 2);
            tBang2.transitionStatus(DeliveryStatus.DISPATCHED); // Dispatched
            deliveryTaskRepository.save(tBang2);

            tBang1.assignToRoute(rBang, 3);
            tBang1.transitionStatus(DeliveryStatus.DISPATCHED); // Dispatched
            deliveryTaskRepository.save(tBang1);

            // Update Vehicle coordinates to show active movement starting from Task 3
            v3.updateLocation(12.9360, 77.6235); // Close to Koramangala
            vehicleRepository.save(v3);


            // --- Route 2: Mumbai Hub (COMPLETED ROUTE) ---
            // Depot: Mumbai Depot (19.0760, 72.8777)
            Route rMum = new Route("RT-MUMBAI-01", v2, d2, 19.0760, 72.8777);
            rMum.setOptimizationResult(
                38.2,  // distance
                110,   // duration minutes
                4.78,  // fuel liters
                String.format("[%d,%d,%d]", tMum2.getId(), tMum1.getId(), tMum3.getId())
            );
            rMum.setRouteScore(94.2);
            rMum.dispatch(); // dispatch first
            rMum.complete(); // complete route, sets completedAt = Instant.now()
            rMum = routeRepository.save(rMum);

            // Assign & Complete Tasks for Mumbai Route
            tMum2.assignToRoute(rMum, 1);
            tMum2.transitionStatus(DeliveryStatus.DISPATCHED);
            tMum2.transitionStatus(DeliveryStatus.IN_TRANSIT);
            tMum2.transitionStatus(DeliveryStatus.DELIVERED);
            deliveryTaskRepository.save(tMum2);

            tMum1.assignToRoute(rMum, 2);
            tMum1.transitionStatus(DeliveryStatus.DISPATCHED);
            tMum1.transitionStatus(DeliveryStatus.IN_TRANSIT);
            tMum1.transitionStatus(DeliveryStatus.DELIVERED);
            deliveryTaskRepository.save(tMum1);

            tMum3.assignToRoute(rMum, 3);
            tMum3.transitionStatus(DeliveryStatus.DISPATCHED);
            tMum3.transitionStatus(DeliveryStatus.IN_TRANSIT);
            tMum3.transitionStatus(DeliveryStatus.DELIVERED);
            deliveryTaskRepository.save(tMum3);


            // --- Route 3: Hyderabad Hub (ACTIVE ROUTE - Animating immediately!) ---
            // Depot: Hyderabad Depot (17.3850, 78.4867)
            Route rHyd = new Route("RT-HYDERABAD-01", v4, d4, 17.3850, 78.4867);
            rHyd.setOptimizationResult(
                28.6,
                82,
                5.72,  // Electric kWh consumption equivalent
                String.format("[%d,%d,%d]", tHyd1.getId(), tHyd3.getId(), tHyd2.getId())
            );
            rHyd.setRouteScore(97.8);
            rHyd.dispatch(); // Transitions status to ACTIVE
            rHyd = routeRepository.save(rHyd);

            // Assign Tasks to Hyderabad Route with active tracking states
            tHyd1.assignToRoute(rHyd, 1);
            tHyd1.transitionStatus(DeliveryStatus.DISPATCHED);
            tHyd1.transitionStatus(DeliveryStatus.IN_TRANSIT); // Underway
            deliveryTaskRepository.save(tHyd1);

            tHyd3.assignToRoute(rHyd, 2);
            tHyd3.transitionStatus(DeliveryStatus.DISPATCHED);
            deliveryTaskRepository.save(tHyd3);

            tHyd2.assignToRoute(rHyd, 3);
            tHyd2.transitionStatus(DeliveryStatus.DISPATCHED);
            deliveryTaskRepository.save(tHyd2);

            // Update Vehicle coordinates to show active movement near HITEC City
            v4.updateLocation(17.4390, 78.3840);
            vehicleRepository.save(v4);


            // --- Route 4: Pune Hub (PLANNED ROUTE) ---
            // Depot: Pune Depot (18.5204, 73.8567)
            Route rPune = new Route("RT-PUNE-01", v1, d1, 18.5204, 73.8567);
            rPune.setOptimizationResult(
                19.8,
                55,
                2.48, // Fuel liters
                String.format("[%d,%d]", tPune1.getId(), tPune3.getId())
            );
            rPune.setRouteScore(98.1);
            rPune = routeRepository.save(rPune);

            // Assign Tasks to Planned Pune Route
            tPune1.assignToRoute(rPune, 1);
            tPune1.transitionStatus(DeliveryStatus.DISPATCHED);
            deliveryTaskRepository.save(tPune1);
            tPune3.assignToRoute(rPune, 2);
            tPune3.transitionStatus(DeliveryStatus.DISPATCHED);
            deliveryTaskRepository.save(tPune3);


            // --- Route 5: Chennai Hub (PLANNED ROUTE) ---
            // Depot: Chennai Depot (13.0827, 80.2707)
            Route rChe = new Route("RT-CHENNAI-01", v5, d5, 13.0827, 80.2707);
            rChe.setOptimizationResult(
                31.2,
                90,
                6.24, // Electric kWh equivalent
                String.format("[%d,%d,%d]", tChe3.getId(), tChe1.getId(), tChe4.getId())
            );
            rChe.setRouteScore(95.0);
            rChe = routeRepository.save(rChe);

            // Assign Tasks to Chennai Route
            tChe3.assignToRoute(rChe, 1);
            tChe3.transitionStatus(DeliveryStatus.DISPATCHED);
            deliveryTaskRepository.save(tChe3);
            tChe1.assignToRoute(rChe, 2);
            tChe1.transitionStatus(DeliveryStatus.DISPATCHED);
            deliveryTaskRepository.save(tChe1);
            tChe4.assignToRoute(rChe, 3);
            tChe4.transitionStatus(DeliveryStatus.DISPATCHED);
            deliveryTaskRepository.save(tChe4);

            System.out.println("SUCCESS: Database Seeding is complete! Relational constraints successfully maintained.");
        };
    }
}
