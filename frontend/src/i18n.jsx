import React, { createContext, useContext, useState, useEffect } from 'react';

const translations = {
  en: {
    fleet_dispatcher: 'Fleet Dispatcher',
    hero_description: 'Professional Fleet Registry, pluggable strategy route scoring, and live STOMP WebSocket coordinate simulation.',
    welcome_back: 'Welcome back',
    sign_in_terminal: 'Sign in to your dispatcher terminal',
    quick_demo_access: 'Quick Demo Access (1-Click Fill)',
    admin_btn: 'ADMIN',
    dispatcher_btn: 'DISPATCHER',
    driver_btn: 'DRIVER',
    username: 'Username',
    password: 'Password',
    dont_have_account: "Don't have an account?",
    register_dispatcher: 'Register Dispatcher',
    create_account: 'Create Account',
    register_credentials: 'Register dispatcher credentials',
    full_name: 'Full Name',
    role: 'Role',
    dispatcher: 'Dispatcher',
    administrator: 'Administrator',
    email: 'Email',
    contact_number: 'Contact Number',
    already_have_account: 'Already have an account?',
    sign_in: 'Sign In',
    fleet_console: 'Fleet Console',
    refresh: 'Refresh',
    sign_out: 'Sign Out',
    registered_trucks: 'Registered Trucks',
    operational: 'Operational',
    in_maintenance: 'In Maintenance',
    total_drivers: 'Total Drivers',
    fleet_operations_summary: 'Fleet Operations Summary',
    available_drivers: 'Available Drivers',
    drivers_on_route: 'Drivers On Route',
    pending_packages: 'Pending Packages',
    in_transit: 'In Transit',
    delivered_today: 'Delivered Today',
    active_routes: 'Active Routes',
    vehicle_registry: 'Vehicle Registry',
    all_statuses: 'All Statuses',
    scheduled_maintenance: 'Scheduled Maintenance',
    diesel: 'Diesel',
    petrol: 'Petrol',
    cng: 'CNG',
    electric: 'Electric',
    submit: 'Submit',
    weight_capacity: 'Weight Capacity',
    vol_capacity: 'Vol Capacity',
    fuel_type: 'Fuel Type',
    odometer: 'Odometer',
    no_matching_vehicles: 'No matching vehicles found.',
    prev: 'Prev',
    next: 'Next',
    driver_manifest_registry: 'Driver Manifest Registry',
    available: 'Available',
    on_route: 'On Route',
    license_expiry: 'License Expiry',
    duty_shift: 'Duty Shift',
    phone: 'Phone',
    vehicle: 'Vehicle',
    unassigned: 'Unassigned',
    no_matching_drivers: 'No matching drivers found.',
    delivery_registry_stop_orders: 'Delivery Registry Stop Orders',
    dispatched: 'Dispatched',
    delivered: 'Delivered',
    failed: 'Failed',
    window_start: 'Window Start',
    window_end: 'Window End',
    create_stop: 'Create Stop',
    destination_address: 'Destination Address',
    recipient: 'Recipient',
    coordinates: 'Coordinates',
    weight_kg: 'Weight (kg)',
    time_window: 'Time Window',
    status: 'Status',
    route_id: 'Route ID',
    actions: 'Actions',
    no_delivery_tasks: 'No delivery tasks found.',
    optimized_delivery_routes: 'Optimized Delivery Routes',
    planned: 'Planned',
    active: 'Active',
    completed: 'Completed',
    planner_instructions: 'Select unassigned delivery stops, then choose a compatible truck, driver, and departure time. The planner checks capacity, driver license, assigned vehicle, shift, and delivery windows before optimizing.',
    no_unassigned_stops: 'No unassigned delivery stops available.',
    stops: 'Stops',
    total_weight: 'Total Weight',
    total_volume: 'Total Volume',
    select_operational_vehicle: 'Select Operational Vehicle',
    choose_vehicle: 'Choose Vehicle',
    select_available_driver: 'Select Available Driver',
    choose_driver: 'Choose Driver',
    planned_departure: 'Planned Departure',
    score: 'Score',
    dist_km: 'Dist (km)',
    dur_min: 'Dur (min)',
    fuel_l: 'Fuel (L)',
    deliver: 'Deliver',
    fail: 'Fail',
    no_routes_optimized: 'No routes optimized yet.',
    signing_in: 'Signing in...',
    registering: 'Registering...',
    register_btn: 'Register',
    cancel: 'Cancel',
    register_vehicle_btn: 'Register Vehicle',
    register_driver_btn: 'Register Driver',
    new_outbound_stop_btn: 'New Outbound Stop',
    plan_optimized_route_btn: 'Plan Optimized Route',
    calculate_route_btn: 'Calculate Route',
    dispatch_btn: 'Dispatch',
    complete_route_btn: 'Complete Route',
    hide_stops_btn: 'Hide stops',
    manifest_list_btn: 'Manifest list',
    service_required_btn: 'Service Required',
    mark_restored_btn: 'Mark Restored',
    matrix_optimization: 'Pluggable Strategy Optimization Matrix',
    score_analysis: 'Dynamic Route Score Analysis',
    stomp_simulations: 'STOMP Live Coordinate Simulations',
    audit_logs: 'Propagation-Independent Audit Logs',
    dashboard: 'Dashboard',
    fleet_registry: 'Fleet Registry',
    deliveries: 'Deliveries',
    route_planner: 'Route Planner'
  },
  hi: {
    fleet_dispatcher: 'फ्लीट डिस्पैचर',
    hero_description: 'व्यावसायिक फ्लीट रजिस्ट्री, प्लगेबल रणनीति रूट स्कोरिंग, और लाइव स्टॉम्प (STOMP) वेबसॉकेट समन्वय सिमुलेशन।',
    welcome_back: 'आपका स्वागत है',
    sign_in_terminal: 'अपने डिस्पैचर टर्मिनल में साइन इन करें',
    quick_demo_access: 'त्वरित डेमो एक्सेस (1-क्लिक फिल)',
    admin_btn: 'एडमिन',
    dispatcher_btn: 'डिस्पैचर',
    driver_btn: 'ड्राइवर',
    username: 'यूज़रनेम',
    password: 'पासवर्ड',
    dont_have_account: 'खाता नहीं है?',
    register_dispatcher: 'डिस्पैचर रजिस्टर करें',
    create_account: 'खाता बनाएं',
    register_credentials: 'डिस्पैचर क्रेडेंशियल रजिस्टर करें',
    full_name: 'पूरा नाम',
    role: 'भूमिका',
    dispatcher: 'डिस्पैचर',
    administrator: 'प्रशासक',
    email: 'ईमेल',
    contact_number: 'संपर्क नंबर',
    already_have_account: 'पहले से ही एक खाता है?',
    sign_in: 'साइन इन करें',
    fleet_console: 'फ्लीट कंसोल',
    refresh: 'रिफ्रेश',
    sign_out: 'साइन आउट',
    registered_trucks: 'पंजीकृत ट्रक',
    operational: 'सक्रिय',
    in_maintenance: 'रखरखाव में',
    total_drivers: 'कुल ड्राइवर',
    fleet_operations_summary: 'फ्लीट संचालन सारांश',
    available_drivers: 'उपलब्ध ड्राइवर',
    drivers_on_route: 'मार्ग पर ड्राइवर',
    pending_packages: 'लंबित पैकेज',
    in_transit: 'मार्ग में',
    delivered_today: 'आज वितरित',
    active_routes: 'सक्रिय मार्ग',
    vehicle_registry: 'वाहन रजिस्ट्री',
    all_statuses: 'सभी स्थितियाँ',
    scheduled_maintenance: 'अनुसूचित रखरखाव',
    diesel: 'डीजल',
    petrol: 'पेट्रोल',
    cng: 'सीएनजी',
    electric: 'इलेक्ट्रिक',
    submit: 'जमा करें',
    weight_capacity: 'वजन क्षमता',
    vol_capacity: 'आयतन क्षमता',
    fuel_type: 'ईंधन प्रकार',
    odometer: 'ओडोमीटर',
    no_matching_vehicles: 'कोई मिलान वाहन नहीं मिला।',
    prev: 'पिछला',
    next: 'अगला',
    driver_manifest_registry: 'ड्राइवर मैनिफेस्ट रजिस्ट्री',
    available: 'उपलब्ध',
    on_route: 'मार्ग पर',
    license_expiry: 'लाइसेंस समाप्ति',
    duty_shift: 'ड्यूटी शिफ्ट',
    phone: 'फ़ोन',
    vehicle: 'वाहन',
    unassigned: 'अनिर्धारित',
    no_matching_drivers: 'कोई मिलान ड्राइवर नहीं मिला।',
    delivery_registry_stop_orders: 'वितरण रजिस्ट्री स्टॉप ऑर्डर',
    dispatched: 'भेजा गया',
    delivered: 'वितरित',
    failed: 'विफल',
    window_start: 'विंडो प्रारंभ',
    window_end: 'विंडो समाप्ति',
    create_stop: 'स्टॉप बनाएं',
    destination_address: 'गंतव्य पता',
    recipient: 'प्राप्तकर्ता',
    coordinates: 'निर्देशांक',
    weight_kg: 'वजन (किग्रा)',
    time_window: 'समय विंडो',
    status: 'स्थिति',
    route_id: 'मार्ग आईडी',
    actions: 'कार्रवाई',
    no_delivery_tasks: 'कोई वितरण कार्य नहीं मिला।',
    optimized_delivery_routes: 'अनुकूलित वितरण मार्ग',
    planned: 'नियोजित',
    active: 'सक्रिय',
    completed: 'पूरा किया गया',
    planner_instructions: 'अनिर्धारित वितरण स्टॉप चुनें, फिर एक संगत ट्रक, ड्राइवर और प्रस्थान समय चुनें। योजनाकार अनुकूलन करने से पहले क्षमता, ड्राइवर लाइसेंस, सौंपे गए वाहन, शिफ्ट और वितरण समय खिड़की की जांच करता है।',
    no_unassigned_stops: 'कोई अनिर्धारित वितरण स्टॉप उपलब्ध नहीं है।',
    stops: 'स्टॉप',
    total_weight: 'कुल वजन',
    total_volume: 'कुल आयतन',
    select_operational_vehicle: 'सक्रिय वाहन चुनें',
    choose_vehicle: 'वाहन चुनें',
    select_available_driver: 'उपलब्ध ड्राइवर चुनें',
    choose_driver: 'ड्राइवर चुनें',
    planned_departure: 'नियोजित प्रस्थान',
    score: 'स्कोर',
    dist_km: 'दूरी (किमी)',
    dur_min: 'अवधि (मिनट)',
    fuel_l: 'ईंधन (लीटर)',
    deliver: 'वितरित करें',
    fail: 'विफल करें',
    no_routes_optimized: 'अभी तक कोई मार्ग अनुकूलित नहीं किया गया है।',
    signing_in: 'साइन इन हो रहा है...',
    registering: 'रजिस्टर हो रहा है...',
    register_btn: 'रजिस्टर करें',
    cancel: 'रद्द करें',
    register_vehicle_btn: 'वाहन पंजीकृत करें',
    register_driver_btn: 'ड्राइवर पंजीकृत करें',
    new_outbound_stop_btn: 'नया आउटबाउंड स्टॉप',
    plan_optimized_route_btn: 'अनुकूलित मार्ग की योजना बनाएं',
    calculate_route_btn: 'मार्ग की गणना करें',
    dispatch_btn: 'रवाना करें',
    complete_route_btn: 'मार्ग पूरा करें',
    hide_stops_btn: 'स्टॉप छुपाएं',
    manifest_list_btn: 'मैनिफेस्ट सूची',
    service_required_btn: 'सेवा आवश्यक',
    mark_restored_btn: 'सक्रिय चिह्नित करें',
    matrix_optimization: 'प्लगेबल रणनीति अनुकूलन मैट्रिक्स',
    score_analysis: 'गतिशील मार्ग स्कोर विश्लेषण',
    stomp_simulations: 'स्टॉम्प लाइव समन्वय सिमुलेशन',
    audit_logs: 'प्रसार-स्वतंत्र ऑडिट लॉग',
    dashboard: 'डैशबोर्ड',
    fleet_registry: 'फ्लीट रजिस्ट्री',
    deliveries: 'वितरण',
    route_planner: 'मार्ग योजनाकार'
  }
};

const ALLOWED_LANGS = ['en', 'hi'];

// Programmatically convert translation objects to secure ES6 Maps at startup
// to completely prevent any bracket notation lookup or prototype pollution warnings.
const translationMaps = {
  en: new Map(Object.entries(translations.en)),
  hi: new Map(Object.entries(translations.hi))
};

const LanguageContext = createContext();

export function LanguageProvider({ children }) {
  const [lang, setLang] = useState(() => {
    const stored = localStorage.getItem('fleet-lang');
    return stored === 'hi' ? 'hi' : 'en';
  });

  useEffect(() => {
    localStorage.setItem('fleet-lang', lang);
  }, [lang]);

  const t = (key) => {
    if (typeof key !== 'string') return '';
    
    // Explicit condition checks to avoid dynamic translations[lang] bracket lookup
    let currentMap = translationMaps.en;
    if (lang === 'hi') {
      currentMap = translationMaps.hi;
    }

    // Secure Map.get retrieval avoids standard prototype pollution vulnerabilities
    if (currentMap.has(key)) {
      return currentMap.get(key);
    }
    if (translationMaps.en.has(key)) {
      return translationMaps.en.get(key);
    }
    return key;
  };

  const changeLanguage = (newLang) => {
    if (newLang === 'en' || newLang === 'hi') {
      setLang(newLang);
    }
  };

  return (
    <LanguageContext.Provider value={{ t, lang, changeLanguage }}>
      {children}
    </LanguageContext.Provider>
  );
}



export function useTranslation() {
  const context = useContext(LanguageContext);
  if (!context) {
    throw new Error('useTranslation must be used within a LanguageProvider');
  }
  return context;
}
