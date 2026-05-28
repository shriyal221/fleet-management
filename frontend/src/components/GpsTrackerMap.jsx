import React from 'react';
import { MapPin, Navigation, Truck } from 'lucide-react';

const BENGALURU_BOUNDS = {
  minLat: 12.82,
  maxLat: 13.02,
  minLng: 77.52,
  maxLng: 77.76
};

function getXY(lat, lng) {
  const { minLat, maxLat, minLng, maxLng } = BENGALURU_BOUNDS;
  const x = ((lng - minLng) / (maxLng - minLng)) * 100;
  const y = 100 - ((lat - minLat) / (maxLat - minLat)) * 100; // Invert Y for screen coordinates
  return {
    x: Math.max(5, Math.min(95, x)) + '%',
    y: Math.max(5, Math.min(95, y)) + '%'
  };
}

export function GpsTrackerMap({ vehicles, deliveryTasks, routes }) {
  // Find active routes
  const activeRoutes = routes.filter(r => r.status === 'ACTIVE');

  // Filter tasks that are on active routes or unassigned
  const stops = deliveryTasks.filter(t => t.deliveryStatus !== 'UNASSIGNED');

  return (
    <div style={{ display: 'grid', gridTemplateColumns: '1fr 280px', gap: '20px', marginTop: '24px' }}>
      {/* Map Canvas */}
      <div className="panel" style={{
        height: '450px',
        position: 'relative',
        background: 'rgba(15, 23, 42, 0.7)',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        overflow: 'hidden',
        display: 'flex',
        flexDirection: 'column'
      }}>
        <div style={{ padding: '0 0 12px 0', borderBottom: '1px solid rgba(255,255,255,0.05)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h4 style={{ color: '#fff', fontSize: '1rem' }}>Live GPS Fleet Radar</h4>
          <span style={{ fontSize: '0.75rem', color: '#10b981', display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#10b981', animation: 'ping 1.5s infinite' }} />
            Radar Connection Active
          </span>
        </div>

        <div style={{ flex: 1, position: 'relative', width: '100%', height: '100%', overflow: 'hidden' }}>
          {/* Bengaluru Grid background */}
          <div style={{
            position: 'absolute',
            inset: 0,
            backgroundImage: 'radial-gradient(rgba(14, 165, 233, 0.12) 1.5px, transparent 1.5px)',
            backgroundSize: '24px 24px',
            pointerEvents: 'none'
          }} />

          {/* Central Depot */}
          {(() => {
            const pos = getXY(12.9716, 77.5946);
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
                  width: '16px',
                  height: '16px',
                  borderRadius: '50%',
                  background: '#0ea5e9',
                  border: '3px solid #fff',
                  boxShadow: '0 0 15px #0ea5e9',
                  animation: 'pulse 2s infinite'
                }} />
                <span style={{ fontSize: '0.65rem', color: '#38bdf8', fontWeight: 'bold', display: 'block', marginTop: '4px', textShadow: '0 2px 4px rgba(0,0,0,0.8)' }}>DEPOT</span>
              </div>
            );
          })()}

          {/* Active Delivery Stop Markers */}
          {stops.map(stop => {
            const pos = getXY(stop.latitude, stop.longitude);
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
                <MapPin size={18} color={markerColor} style={{
                  filter: `drop-shadow(0 2px 5px ${markerColor})`
                }} />
                <span style={{
                  position: 'absolute',
                  left: '100%',
                  top: '0',
                  whiteSpace: 'nowrap',
                  fontSize: '0.6rem',
                  color: '#e2e8f0',
                  background: 'rgba(15,23,42,0.85)',
                  padding: '2px 4px',
                  borderRadius: '4px',
                  border: `1px solid ${markerColor}44`,
                  marginLeft: '4px',
                  pointerEvents: 'none'
                }}>
                  #{stop.id}
                </span>
              </div>
            );
          })}

          {/* Animated Active Vehicles */}
          {vehicles.map(v => {
            if (!v.currentLatitude || !v.currentLongitude) return null;
            
            // Check if vehicle has active route
            const activeRoute = activeRoutes.find(r => r.vehicleId === v.id);
            if (!activeRoute) return null;

            const pos = getXY(v.currentLatitude, v.currentLongitude);

            return (
              <div key={v.id} style={{
                position: 'absolute',
                left: pos.x,
                top: pos.y,
                transform: 'translate(-50%, -50%)',
                zIndex: 5,
                transition: 'left 2.5s cubic-bezier(0.25, 0.46, 0.45, 0.94), top 2.5s cubic-bezier(0.25, 0.46, 0.45, 0.94)',
                textAlign: 'center'
              }}>
                <div style={{
                  width: '32px',
                  height: '32px',
                  borderRadius: '50%',
                  background: '#6366f1',
                  border: '2px solid #fff',
                  boxShadow: '0 4px 15px rgba(99, 102, 241, 0.6)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  animation: 'bounce 1s infinite alternate'
                }}>
                  <Truck size={16} color="#fff" />
                </div>
                <span style={{
                  fontSize: '0.65rem',
                  color: '#fff',
                  fontWeight: 'bold',
                  background: 'rgba(99, 102, 241, 0.95)',
                  padding: '1px 6px',
                  borderRadius: '10px',
                  display: 'block',
                  marginTop: '4px',
                  whiteSpace: 'nowrap',
                  boxShadow: '0 2px 4px rgba(0,0,0,0.5)'
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
        height: '450px',
        overflowY: 'auto',
        background: 'rgba(15, 23, 42, 0.45)',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        display: 'flex',
        flexDirection: 'column',
        gap: '12px'
      }}>
        <h4 style={{ color: '#fff', fontSize: '1rem', borderBottom: '1px solid rgba(255,255,255,0.05)', paddingBottom: '8px' }}>Active Fleet Shifts</h4>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', flex: 1 }}>
          {activeRoutes.length > 0 ? (
            activeRoutes.map(r => {
              const vehicle = vehicles.find(v => v.id === r.vehicleId);
              return (
                <div key={r.id} style={{
                  padding: '10px 12px',
                  borderRadius: '8px',
                  background: 'rgba(255,255,255,0.02)',
                  borderLeft: '4px solid #6366f1',
                  border: '1px solid rgba(255,255,255,0.05)'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                    <span style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#fff' }}>{r.routeName}</span>
                    <span className="status on-route" style={{ fontSize: '0.65rem', padding: '2px 8px' }}>ACTIVE</span>
                  </div>
                  <div style={{ fontSize: '0.72rem', color: '#cbd5e1', display: 'flex', flexDirection: 'column', gap: '3px' }}>
                    <div><strong>Truck:</strong> {r.vehiclePlate}</div>
                    <div><strong>Driver:</strong> {r.driverName}</div>
                    {vehicle && vehicle.currentLatitude && (
                      <div style={{ color: '#0ea5e9', display: 'flex', alignItems: 'center', gap: '4px', marginTop: '2px' }}>
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
              <Truck size={32} style={{ marginBottom: '8px', opacity: 0.3 }} />
              No active routes on tracking duty
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
