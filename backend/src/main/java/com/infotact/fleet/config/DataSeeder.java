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

            // Vehicle 6: Heavy Truck (Diesel) - Delhi Hub
            Vehicle v6 = new Vehicle("DL-01-AB-1111", "Ashok Leyland", "Boss", 2023, 5000.0, 20.0, "DIESEL");
            v6.updateLocation(28.7041, 77.1025);
            v6.addOdometerKm(22000.0);
            vehicleRepository.save(v6);

            // Vehicle 7: Delivery Van (CNG) - Kolkata Hub
            Vehicle v7 = new Vehicle("WB-02-CD-2222", "Maruti", "Super Carry", 2024, 750.0, 3.5, "CNG");
            v7.updateLocation(22.5726, 88.3639);
            v7.addOdometerKm(4500.0);
            vehicleRepository.save(v7);

            // Vehicle 8: Refrigerated Truck (Diesel) - Ahmedabad Hub
            Vehicle v8 = new Vehicle("GJ-01-EF-3333", "Eicher", "Pro 2049", 2022, 2000.0, 10.0, "DIESEL");
            v8.updateLocation(23.0225, 72.5714);
            v8.addOdometerKm(34000.0);
            vehicleRepository.save(v8);

            // Vehicle 9: Mini Transport EV (Electric) - Jaipur Hub
            Vehicle v9 = new Vehicle("RJ-14-GH-4444", "Mahindra", "Treo Zor", 2025, 550.0, 2.5, "ELECTRIC");
            v9.updateLocation(26.9124, 75.7873);
            v9.addOdometerKm(1200.0);
            vehicleRepository.save(v9);

            // Vehicle 10: Cargo Truck (Diesel) - Lucknow Hub
            Vehicle v10 = new Vehicle("UP-32-IJ-5555", "Tata", "LPT 1918", 2023, 10000.0, 30.0, "DIESEL");
            v10.updateLocation(26.8467, 80.9462);
            v10.addOdometerKm(18000.0);
            vehicleRepository.save(v10);

            // Vehicle 11-15: Additional Vehicles
            Vehicle v11 = new Vehicle("MP-09-KL-6666", "Tata", "Yodha", 2024, 1200.0, 8.0, "DIESEL");
            v11.updateLocation(22.7196, 75.8577);
            v11.addOdometerKm(5000.0);
            vehicleRepository.save(v11);

            Vehicle v12 = new Vehicle("CH-01-MN-7777", "Mahindra", "Supro", 2023, 800.0, 5.0, "CNG");
            v12.updateLocation(30.7333, 76.7794);
            v12.addOdometerKm(15000.0);
            vehicleRepository.save(v12);

            Vehicle v13 = new Vehicle("KL-01-OP-8888", "Eicher", "Pro 2059", 2022, 3500.0, 12.0, "DIESEL");
            v13.updateLocation(8.5241, 76.9366);
            v13.addOdometerKm(45000.0);
            vehicleRepository.save(v13);

            Vehicle v14 = new Vehicle("PB-10-QR-9999", "Tata", "Signa 1923.K", 2025, 12000.0, 35.0, "DIESEL");
            v14.updateLocation(30.9010, 75.8573);
            v14.addOdometerKm(800.0);
            vehicleRepository.save(v14);

            Vehicle v15 = new Vehicle("BR-01-ST-0000", "Ashok Leyland", "Partner", 2024, 2500.0, 9.0, "CNG");
            v15.updateLocation(25.5941, 85.1376);
            v15.addOdometerKm(12000.0);
            vehicleRepository.save(v15);

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

            // Driver 6: Amit Singh (Delhi)
            Driver d6 = new Driver("Amit Singh", "+91 91000 11111", "amit.singh@fleetpro.com", "DL-DL-2022-11111", Instant.now().plus(500, ChronoUnit.DAYS), LocalTime.of(8, 0), LocalTime.of(18, 0));
            d6.assignVehicle(v6);
            d6.updateStatus(DriverStatus.AVAILABLE);
            driverRepository.save(d6);

            // Driver 7: Bimal Das (Kolkata)
            Driver d7 = new Driver("Bimal Das", "+91 92000 22222", "bimal.das@fleetpro.com", "WB-DL-2021-22222", Instant.now().plus(400, ChronoUnit.DAYS), LocalTime.of(9, 0), LocalTime.of(19, 0));
            d7.assignVehicle(v7);
            d7.updateStatus(DriverStatus.AVAILABLE);
            driverRepository.save(d7);

            // Driver 8: Chirag Patel (Ahmedabad)
            Driver d8 = new Driver("Chirag Patel", "+91 93000 33333", "chirag.patel@fleetpro.com", "GJ-DL-2020-33333", Instant.now().plus(300, ChronoUnit.DAYS), LocalTime.of(7, 0), LocalTime.of(17, 0));
            d8.assignVehicle(v8);
            d8.updateStatus(DriverStatus.AVAILABLE);
            driverRepository.save(d8);

            // Driver 9: Deepak Sharma (Jaipur)
            Driver d9 = new Driver("Deepak Sharma", "+91 94000 44444", "deepak.sharma@fleetpro.com", "RJ-DL-2023-44444", Instant.now().plus(700, ChronoUnit.DAYS), LocalTime.of(10, 0), LocalTime.of(20, 0));
            d9.assignVehicle(v9);
            d9.updateStatus(DriverStatus.AVAILABLE);
            driverRepository.save(d9);

            // Driver 10: Eshan Tiwari (Lucknow)
            Driver d10 = new Driver("Eshan Tiwari", "+91 95000 55555", "eshan.tiwari@fleetpro.com", "UP-DL-2019-55555", Instant.now().plus(200, ChronoUnit.DAYS), LocalTime.of(6, 0), LocalTime.of(16, 0));
            d10.assignVehicle(v10);
            d10.updateStatus(DriverStatus.AVAILABLE);
            driverRepository.save(d10);

            // Driver 11-15: Additional Drivers
            Driver d11 = new Driver("Manoj Tiwari", "+91 96000 66666", "manoj.tiwari@fleetpro.com", "MP-DL-2020-66666", Instant.now().plus(450, ChronoUnit.DAYS), LocalTime.of(8, 0), LocalTime.of(18, 0));
            d11.assignVehicle(v11);
            d11.updateStatus(DriverStatus.AVAILABLE);
            driverRepository.save(d11);

            Driver d12 = new Driver("Harpreet Singh", "+91 97000 77777", "harpreet.singh@fleetpro.com", "CH-DL-2021-77777", Instant.now().plus(350, ChronoUnit.DAYS), LocalTime.of(9, 0), LocalTime.of(19, 0));
            d12.assignVehicle(v12);
            d12.updateStatus(DriverStatus.AVAILABLE);
            driverRepository.save(d12);

            Driver d13 = new Driver("Ramesh Nair", "+91 98000 88888", "ramesh.nair@fleetpro.com", "KL-DL-2019-88888", Instant.now().plus(600, ChronoUnit.DAYS), LocalTime.of(7, 0), LocalTime.of(17, 0));
            d13.assignVehicle(v13);
            d13.updateStatus(DriverStatus.AVAILABLE);
            driverRepository.save(d13);

            Driver d14 = new Driver("Gurdeep Gill", "+91 99000 99999", "gurdeep.gill@fleetpro.com", "PB-DL-2023-99999", Instant.now().plus(800, ChronoUnit.DAYS), LocalTime.of(6, 0), LocalTime.of(16, 0));
            d14.assignVehicle(v14);
            d14.updateStatus(DriverStatus.AVAILABLE);
            driverRepository.save(d14);

            Driver d15 = new Driver("Nitish Kumar", "+91 90000 00000", "nitish.kumar@fleetpro.com", "BR-DL-2022-00000", Instant.now().plus(550, ChronoUnit.DAYS), LocalTime.of(10, 0), LocalTime.of(20, 0));
            d15.assignVehicle(v15);
            d15.updateStatus(DriverStatus.AVAILABLE);
            driverRepository.save(d15);

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

            // --- Delhi Tasks ---
            DeliveryTask tDel1 = new DeliveryTask("Connaught Place", "Tech Store", "+91 11 1111 1111", 28.6304, 77.2177, 20.0, 0.5, Instant.now().plus(1, ChronoUnit.HOURS), Instant.now().plus(4, ChronoUnit.HOURS), "Electronics");
            DeliveryTask tDel2 = new DeliveryTask("Karol Bagh", "Fashion Hub", "+91 11 2222 2222", 28.6515, 77.1902, 15.0, 0.3, Instant.now().plus(2, ChronoUnit.HOURS), Instant.now().plus(5, ChronoUnit.HOURS), "Garments");
            DeliveryTask tDel3 = new DeliveryTask("Nehru Place", "Computers Ltd", "+91 11 3333 3333", 28.5494, 77.2526, 50.0, 1.2, Instant.now().plus(1, ChronoUnit.HOURS), Instant.now().plus(6, ChronoUnit.HOURS), "Laptops");
            DeliveryTask tDel4 = new DeliveryTask("Chandni Chowk", "Spice Market", "+91 11 4444 4444", 28.6505, 77.2303, 10.0, 0.2, Instant.now().plus(3, ChronoUnit.HOURS), Instant.now().plus(7, ChronoUnit.HOURS), "Spices");
            DeliveryTask tDel5 = new DeliveryTask("South Ex", "Luxury Goods", "+91 11 5555 5555", 28.5682, 77.2201, 8.0, 0.15, Instant.now().plus(2, ChronoUnit.HOURS), Instant.now().plus(5, ChronoUnit.HOURS), "Watches");
            
            deliveryTaskRepository.save(tDel1);
            deliveryTaskRepository.save(tDel2);
            deliveryTaskRepository.save(tDel3);
            deliveryTaskRepository.save(tDel4);
            deliveryTaskRepository.save(tDel5);

            // --- Additional 5 Tasks ---
            DeliveryTask tAdd1 = new DeliveryTask("MG Road, Indore", "Indore Traders", "+91 731 111 1111", 22.7196, 75.8577, 45.0, 1.0, Instant.now().plus(1, ChronoUnit.HOURS), Instant.now().plus(5, ChronoUnit.HOURS), "Textiles");
            DeliveryTask tAdd2 = new DeliveryTask("Sector 17, Chandigarh", "Punjab Electronics", "+91 172 222 2222", 30.7333, 76.7794, 25.0, 0.5, Instant.now().plus(2, ChronoUnit.HOURS), Instant.now().plus(6, ChronoUnit.HOURS), "Home Appliances");
            DeliveryTask tAdd3 = new DeliveryTask("MG Road, Trivandrum", "Kerala Spices", "+91 471 333 3333", 8.5241, 76.9366, 60.0, 1.5, Instant.now().plus(1, ChronoUnit.HOURS), Instant.now().plus(4, ChronoUnit.HOURS), "Bulk Spices");
            DeliveryTask tAdd4 = new DeliveryTask("Ferozepur Road, Ludhiana", "Ludhiana Auto Parts", "+91 161 444 4444", 30.9010, 75.8573, 150.0, 3.0, Instant.now().plus(3, ChronoUnit.HOURS), Instant.now().plus(8, ChronoUnit.HOURS), "Engine Components");
            DeliveryTask tAdd5 = new DeliveryTask("Fraser Road, Patna", "Bihar Books", "+91 612 555 5555", 25.5941, 85.1376, 20.0, 0.4, Instant.now().plus(2, ChronoUnit.HOURS), Instant.now().plus(5, ChronoUnit.HOURS), "Educational Materials");

            deliveryTaskRepository.save(tAdd1);
            deliveryTaskRepository.save(tAdd2);
            deliveryTaskRepository.save(tAdd3);
            deliveryTaskRepository.save(tAdd4);
            deliveryTaskRepository.save(tAdd5);

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

            // --- Route 6: Delhi Hub (PLANNED ROUTE) ---
            Route rDel = new Route("RT-DELHI-01", v6, d6, 28.7041, 77.1025);
            rDel.setOptimizationResult(
                22.5,
                65,
                3.1,
                String.format("[%d,%d,%d]", tDel1.getId(), tDel2.getId(), tDel3.getId())
            );
            rDel.setRouteScore(92.5);
            rDel = routeRepository.save(rDel);
            
            tDel1.assignToRoute(rDel, 1);
            tDel1.transitionStatus(DeliveryStatus.DISPATCHED);
            deliveryTaskRepository.save(tDel1);
            tDel2.assignToRoute(rDel, 2);
            tDel2.transitionStatus(DeliveryStatus.DISPATCHED);
            deliveryTaskRepository.save(tDel2);
            tDel3.assignToRoute(rDel, 3);
            tDel3.transitionStatus(DeliveryStatus.DISPATCHED);
            deliveryTaskRepository.save(tDel3);

            // Note: Since we're adding 5 routes, let's also add simple empty/planned routes for the other 4 vehicles
            Route rKol = new Route("RT-KOLKATA-01", v7, d7, 22.5726, 88.3639);
            rKol.setRouteScore(88.0);
            routeRepository.save(rKol);

            Route rAhm = new Route("RT-AHMEDABAD-01", v8, d8, 23.0225, 72.5714);
            rAhm.setRouteScore(91.2);
            routeRepository.save(rAhm);

            Route rJai = new Route("RT-JAIPUR-01", v9, d9, 26.9124, 75.7873);
            rJai.setRouteScore(95.5);
            routeRepository.save(rJai);

            Route rLuc = new Route("RT-LUCKNOW-01", v10, d10, 26.8467, 80.9462);
            rLuc.setRouteScore(89.4);
            routeRepository.save(rLuc);

            Route rInd = new Route("RT-INDORE-01", v11, d11, 22.7196, 75.8577);
            rInd.setRouteScore(93.0);
            routeRepository.save(rInd);

            Route rCha = new Route("RT-CHANDIGARH-01", v12, d12, 30.7333, 76.7794);
            rCha.setRouteScore(87.5);
            routeRepository.save(rCha);

            Route rTri = new Route("RT-TRIVANDRUM-01", v13, d13, 8.5241, 76.9366);
            rTri.setRouteScore(90.1);
            routeRepository.save(rTri);

            Route rLud = new Route("RT-LUDHIANA-01", v14, d14, 30.9010, 75.8573);
            rLud.setRouteScore(94.8);
            routeRepository.save(rLud);

            Route rPat = new Route("RT-PATNA-01", v15, d15, 25.5941, 85.1376);
            rPat.setRouteScore(89.9);
            routeRepository.save(rPat);

            System.out.println("SUCCESS: Database Seeding is complete! Relational constraints successfully maintained.");
        };
    }
}
