import React from 'react';
import { MapPin, Navigation, Truck } from 'lucide-react';

const CITY_CONFIGS = {
  BANGALORE: {
    name: 'Bengaluru Hub (HQ)',
    bounds: { minLat: 12.82, maxLat: 13.02, minLng: 77.52, maxLng: 77.76 },
    depot: { lat: 12.9716, lng: 77.5946 }
  },
  MUMBAI: {
    name: 'Mumbai Hub',
    bounds: { minLat: 18.90, maxLat: 19.18, minLng: 72.78, maxLng: 72.98 },
    depot: { lat: 19.0760, lng: 72.8777 }
  },
  PUNE: {
    name: 'Pune Hub',
    bounds: { minLat: 18.43, maxLat: 18.63, minLng: 73.75, maxLng: 73.95 },
    depot: { lat: 18.5204, lng: 73.8567 }
  },
  HYDERABAD: {
    name: 'Hyderabad Hub',
    bounds: { minLat: 17.28, maxLat: 17.48, minLng: 78.38, maxLng: 78.58 },
    depot: { lat: 17.3850, lng: 78.4867 }
  },
  CHENNAI: {
    name: 'Chennai Hub',
    bounds: { minLat: 12.98, maxLat: 13.18, minLng: 80.17, maxLng: 80.37 },
    depot: { lat: 13.0827, lng: 80.2707 }
  }
};

function getXY(lat, lng, bounds) {
  const { minLat, maxLat, minLng, maxLng } = bounds;
  const x = ((lng - minLng) / (maxLng - minLng)) * 100;
  const y = 100 - ((lat - minLat) / (maxLat - minLat)) * 100; // Invert Y for screen coordinates
  return {
    x: Math.max(5, Math.min(95, x)) + '%',
    y: Math.max(5, Math.min(95, y)) + '%'
  };
}

export function GpsTrackerMap({ vehicles, deliveryTasks, routes }) {
  const [selectedCity, setSelectedCity] = React.useState('BANGALORE');
  
  // Find active routes
  const activeRoutes = routes.filter(r => r.status === 'ACTIVE');

  // Auto-focus view on city where an active route is currently simulating
  React.useEffect(() => {
    if (activeRoutes.length > 0) {
      const activeRouteName = activeRoutes[0].routeName.toUpperCase();
      if (activeRouteName.includes('BANGALORE')) {
        setSelectedCity('BANGALORE');
      } else if (activeRouteName.includes('MUMBAI')) {
        setSelectedCity('MUMBAI');
      } else if (activeRouteName.includes('PUNE')) {
        setSelectedCity('PUNE');
      } else if (activeRouteName.includes('HYDERABAD')) {
        setSelectedCity('HYDERABAD');
      } else if (activeRouteName.includes('CHENNAI')) {
        setSelectedCity('CHENNAI');
      }
    }
  }, [activeRoutes.length]);

  const validCities = ['BANGALORE', 'MUMBAI', 'PUNE', 'HYDERABAD', 'CHENNAI'];
  const cityKey = validCities.includes(selectedCity) ? selectedCity : 'BANGALORE';
  const activeConfig = CITY_CONFIGS[cityKey];
  const bounds = activeConfig.bounds;

  // Spatial containment helper to filter tasks and vehicles for current view
  const isInsideView = (lat, lng) => {
    return lat >= bounds.minLat && lat <= bounds.maxLat && lng >= bounds.minLng && lng <= bounds.maxLng;
  };

  // Filter tasks that are in-transit/dispatched and within current city bounds
  const stops = deliveryTasks.filter(t => 
    t.deliveryStatus !== 'UNASSIGNED' && 
    t.latitude && t.longitude && 
    isInsideView(t.latitude, t.longitude)
  );

  // Filter active vehicles inside the selected city
  const cityVehicles = vehicles.filter(v => 
    v.currentLatitude && v.currentLongitude && 
    isInsideView(v.currentLatitude, v.currentLongitude)
  );

  return (
    <div style={{ display: 'grid', gridTemplateColumns: '1fr 300px', gap: '20px', marginTop: '24px' }}>
      {/* Map Canvas */}
      <div className="panel" style={{
        height: '480px',
        position: 'relative',
        background: 'rgba(15, 23, 42, 0.75)',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        overflow: 'hidden',
        display: 'flex',
        flexDirection: 'column',
        borderRadius: '12px'
      }}>
        {/* Map Header with city selector */}
        <div style={{ 
          padding: '12px 16px', 
          borderBottom: '1px solid rgba(255,255,255,0.06)', 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center',
          background: 'rgba(30, 41, 59, 0.4)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <h4 style={{ color: '#fff', fontSize: '0.95rem', fontWeight: 700, margin: 0 }}>Live GPS Fleet Radar</h4>
            <select 
              value={selectedCity} 
              onChange={(e) => setSelectedCity(e.target.value)}
              style={{
                background: 'rgba(255,255,255,0.05)',
                border: '1px solid rgba(255,255,255,0.15)',
                borderRadius: '6px',
                color: '#fff',
                fontSize: '0.8rem',
                padding: '4px 28px 4px 10px',
                cursor: 'pointer',
                outline: 'none',
                minWidth: '160px'
              }}
            >
              {Object.entries(CITY_CONFIGS).map(([key, config]) => (
                <option key={key} value={key} style={{ background: '#1e293b', color: '#fff' }}>
                  {config.name}
                </option>
              ))}
            </select>
          </div>
          <span style={{ fontSize: '0.75rem', color: '#10b981', display: 'flex', alignItems: 'center', gap: '6px', fontWeight: 600 }}>
            <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#10b981', display: 'inline-block', boxShadow: '0 0 8px #10b981' }} />
            Radar Connection Active
          </span>
        </div>

        <div style={{ flex: 1, position: 'relative', width: '100%', height: '100%', overflow: 'hidden' }}>
          {/* Grid background */}
          <div style={{
            position: 'absolute',
            inset: 0,
            backgroundImage: 'radial-gradient(rgba(14, 165, 233, 0.15) 1.5px, transparent 1.5px)',
            backgroundSize: '24px 24px',
            pointerEvents: 'none'
          }} />

          {/* Central Depot */}
          {(() => {
            const pos = getXY(activeConfig.depot.lat, activeConfig.depot.lng, bounds);
            return (
              <div style={{
                position: 'absolute',
                left: pos.x,
                top: pos.y,
                transform: 'translate(-50%, -50%)',
                zIndex: 3,
                textAlign: 'center'
              }}>
                <div style={{
                  width: '18px',
                  height: '18px',
                  borderRadius: '50%',
                  background: '#0ea5e9',
                  border: '3px solid #fff',
                  boxShadow: '0 0 20px #0ea5e9, inset 0 0 4px rgba(0,0,0,0.5)',
                  display: 'inline-block'
                }} />
                <span style={{ 
                  fontSize: '0.62rem', 
                  color: '#38bdf8', 
                  fontWeight: 800, 
                  display: 'block', 
                  marginTop: '4px', 
                  textTransform: 'uppercase',
                  letterSpacing: '0.05em',
                  textShadow: '0 2px 4px rgba(0,0,0,0.9)' 
                }}>DEPOT</span>
              </div>
            );
          })()}

          {/* Active Delivery Stop Markers */}
          {stops.map(stop => {
            const pos = getXY(stop.latitude, stop.longitude, bounds);
            const isDelivered = stop.deliveryStatus === 'DELIVERED';
            const isTransit = stop.deliveryStatus === 'IN_TRANSIT';
            const markerColor = isDelivered ? '#10b981' : isTransit ? '#fbbf24' : '#a78bfa';

            return (
              <div key={stop.id} style={{
                position: 'absolute',
                left: pos.x,
                top: pos.y,
                transform: 'translate(-50%, -50%)',
                zIndex: 2,
                transition: 'all 0.5s ease'
              }}>
                <MapPin size={20} color={markerColor} style={{
                  filter: `drop-shadow(0 2px 6px ${markerColor}66)`
                }} />
                <span style={{
                  position: 'absolute',
                  left: '100%',
                  top: '-4px',
                  whiteSpace: 'nowrap',
                  fontSize: '0.6rem',
                  fontWeight: 700,
                  color: '#e2e8f0',
                  background: 'rgba(15,23,42,0.9)',
                  padding: '2px 6px',
                  borderRadius: '4px',
                  border: `1px solid ${markerColor}44`,
                  marginLeft: '4px',
                  pointerEvents: 'none',
                  boxShadow: '0 2px 8px rgba(0,0,0,0.5)'
                }}>
                  #{stop.id} {stop.recipientName ? `- ${stop.recipientName}` : ''}
                </span>
              </div>
            );
          })}

          {/* Animated Active Vehicles */}
          {cityVehicles.map(v => {
            // Find active route matching this vehicle
            const activeRoute = activeRoutes.find(r => r.vehicleId === v.id);
            if (!activeRoute) return null;

            const pos = getXY(v.currentLatitude, v.currentLongitude, bounds);

            return (
              <div key={v.id} style={{
                position: 'absolute',
                left: pos.x,
                top: pos.y,
                transform: 'translate(-50%, -50%)',
                zIndex: 5,
                transition: 'left 3s cubic-bezier(0.25, 0.46, 0.45, 0.94), top 3s cubic-bezier(0.25, 0.46, 0.45, 0.94)',
                textAlign: 'center'
              }}>
                <div style={{
                  width: '36px',
                  height: '36px',
                  borderRadius: '50%',
                  background: '#6366f1',
                  border: '2.5px solid #fff',
                  boxShadow: '0 4px 18px rgba(99, 102, 241, 0.75)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  animation: 'bounce-anim 1.2s infinite alternate'
                }}>
                  <Truck size={18} color="#fff" />
                </div>
                <span style={{
                  fontSize: '0.65rem',
                  color: '#fff',
                  fontWeight: 'bold',
                  background: 'rgba(99, 102, 241, 0.95)',
                  padding: '2px 8px',
                  borderRadius: '12px',
                  display: 'inline-block',
                  marginTop: '4px',
                  whiteSpace: 'nowrap',
                  boxShadow: '0 2px 8px rgba(0,0,0,0.6)',
                  border: '1px solid rgba(255,255,255,0.2)'
                }}>
                  {v.licensePlate}
                </span>
              </div>
            );
          })}
        </div>
      </div>

      {/* Vehicles Sidebar */}
      <div className="panel" style={{
        height: '480px',
        overflowY: 'auto',
        background: 'rgba(15, 23, 42, 0.5)',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        display: 'flex',
        flexDirection: 'column',
        gap: '12px',
        borderRadius: '12px',
        padding: '16px'
      }}>
        <h4 style={{ 
          color: '#fff', 
          fontSize: '0.95rem', 
          fontWeight: 700, 
          margin: '0 0 4px 0', 
          borderBottom: '1px solid rgba(255,255,255,0.06)', 
          paddingBottom: '10px' 
        }}>Active Fleet Shifts</h4>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', flex: 1 }}>
          {activeRoutes.length > 0 ? (
            activeRoutes.map(r => {
              const vehicle = vehicles.find(v => v.id === r.vehicleId);
              return (
                <div key={r.id} style={{
                  padding: '12px 14px',
                  borderRadius: '10px',
                  background: 'rgba(99, 102, 241, 0.04)',
                  borderLeft: '4px solid #6366f1',
                  border: '1px solid rgba(99, 102, 241, 0.15)',
                  boxShadow: '0 2px 6px rgba(0,0,0,0.2)'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                    <span style={{ fontSize: '0.85rem', fontWeight: 800, color: '#fff' }}>{r.routeName}</span>
                    <span className="status on-route" style={{ fontSize: '0.65rem', padding: '2px 8px', fontWeight: 700 }}>ACTIVE</span>
                  </div>
                  <div style={{ fontSize: '0.75rem', color: '#cbd5e1', display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    <div><strong style={{ color: '#94a3b8' }}>Truck:</strong> {r.vehiclePlate}</div>
                    <div><strong style={{ color: '#94a3b8' }}>Driver:</strong> {r.driverName}</div>
                    {vehicle && vehicle.currentLatitude && (
                      <div style={{ color: '#38bdf8', display: 'flex', alignItems: 'center', gap: '4px', marginTop: '4px', fontWeight: 600 }}>
                        <Navigation size={10} style={{ animation: 'spin-anim 4s infinite linear' }} />
                        <span>{vehicle.currentLatitude.toFixed(4)}, {vehicle.currentLongitude.toFixed(4)}</span>
                      </div>
                    )}
                  </div>
                </div>
              );
            })
          ) : (
            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: '#94a3b8', fontSize: '0.8rem', textAlign: 'center', padding: '16px' }}>
              <Truck size={36} style={{ marginBottom: '12px', opacity: 0.3, color: '#6366f1' }} />
              No active routes on tracking duty
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
