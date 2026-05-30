import {
  Truck,
  MapPin,
  Navigation,
  Plus,
  Wrench,
  Users2,
  CheckCircle2,
  RefreshCw,
  LogOut,
  ShieldCheck,
  Eye,
  EyeOff,
  UserPlus,
  Search,
  SlidersHorizontal,
  Compass,
  AlertCircle
} from 'lucide-react';
import { useCallback, useEffect, useMemo, useState, memo } from 'react';
import { apiRequest, API_URL } from './api.js';
import { AnalyticsCharts } from './components/AnalyticsCharts.jsx';
import { GpsTrackerMap } from './components/GpsTrackerMap.jsx';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

function toDateTimeLocalValue(date) {
  const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 16);
}

function formatTimeWindow(start, end) {
  if (!start && !end) return '-';
  const format = (value) => value ? new Date(value).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' }) : 'Open';
  return `${format(start)} to ${format(end)}`;
}

function App() {
  const [auth, setAuth] = useState(() => {
    const stored = localStorage.getItem('fleet-auth');
    return stored ? JSON.parse(stored) : null;
  });
  const [activeTab, setActiveTab] = useState('dashboard');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  
  const [vehicles, setVehicles] = useState([]);
  const [drivers, setDrivers] = useState([]);
  const [deliveryTasks, setDeliveryTasks] = useState([]);
  const [routes, setRoutes] = useState([]);
  const [dashboardData, setDashboardData] = useState(null);

  // Search & Pagination States
  const [vSearch, setVSearch] = useState('');
  const [vStatus, setVStatus] = useState('');
  const [vPage, setVPage] = useState(0);

  const [dSearch, setDSearch] = useState('');
  const [dStatus, setDStatus] = useState('');
  const [dPage, setDPage] = useState(0);

  const [tSearch, setTSearch] = useState('');
  const [tStatus, setTStatus] = useState('');
  const [tPage, setTPage] = useState(0);

  const [rSearch, setRSearch] = useState('');
  const [rStatus, setRStatus] = useState('');
  const [rPage, setRPage] = useState(0);

  const [loginForm, setLoginForm] = useState({ username: '', password: '' });
  const [registerForm, setRegisterForm] = useState({ username: '', password: '', role: 'DISPATCHER', name: '', email: '', contactNumber: '' });
  const [authMode, setAuthMode] = useState('login');
  const [showPassword, setShowPassword] = useState(false);

  const token = auth?.token;
  const userRole = auth?.roles?.[0] || 'DRIVER';

  // Role permissions checks
  const isAdmin = userRole === 'ADMIN';
  const isDispatcher = userRole === 'DISPATCHER';
  const isDriver = userRole === 'DRIVER';

  // Available tabs based on roles
  const tabs = useMemo(() => {
    const allTabs = [
      { id: 'dashboard', label: 'Dashboard', icon: Truck },
      { id: 'fleet', label: 'Fleet Registry', icon: Users2 },
      { id: 'deliveries', label: 'Deliveries', icon: MapPin },
      { id: 'routes', label: 'Route Planner', icon: Navigation }
    ];
    if (isDriver) {
      return allTabs.filter(t => t.id === 'dashboard' || t.id === 'routes');
    }
    return allTabs;
  }, [isDriver]);

  const run = useCallback(async (action, successMsg) => {
    setLoading(true);
    setMessage('');
    try {
      await action();
      if (successMsg) {
        setMessage(successMsg);
      }
    } catch (error) {
      setMessage(error.message || 'Action failed');
    } finally {
      setLoading(false);
    }
  }, []);

  const loadAll = useCallback(async () => {
    await run(async () => {
      // 100% Backwards compatible fetch that includes backend-side searching & pagination
      const [nextVehicles, nextDrivers, nextDeliveries, nextRoutes, nextDashboard] = await Promise.all([
        apiRequest(`/vehicles?search=${vSearch}&status=${vStatus}&page=${vPage}&size=10`, { token }),
        apiRequest(`/drivers?search=${dSearch}&status=${dStatus}&page=${dPage}&size=10`, { token }),
        apiRequest(`/deliveries?search=${tSearch}&status=${tStatus}&page=${tPage}&size=10`, { token }),
        apiRequest(`/routes?search=${rSearch}&status=${rStatus}&page=${rPage}&size=10`, { token }),
        apiRequest('/routes/dashboard', { token })
      ]);
      setVehicles(nextVehicles);
      setDrivers(nextDrivers);
      setDeliveryTasks(nextDeliveries);
      setRoutes(nextRoutes);
      setDashboardData(nextDashboard);
    }, null);
  }, [run, token, vSearch, vStatus, vPage, dSearch, dStatus, dPage, tSearch, tStatus, tPage, rSearch, rStatus, rPage]);

  // Handle live WebSocket coordinates via STOMP broker
  useEffect(() => {
    if (!token) return;

    let refreshTimer = null;

    // Establish WebSocket using SockJS fallback
    const socketUrl = `${API_URL}/ws`.replace('/api', '');
    const client = new Client({
      brokerURL: socketUrl.startsWith('https') ? socketUrl.replace('https', 'wss') : socketUrl.replace('http', 'ws'),
      webSocketFactory: () => new SockJS(socketUrl),
      reconnectDelay: 5000,
      onConnect: () => {
        console.log('radar STOMP active!');
        client.subscribe('/topic/gps', (message) => {
          const payload = JSON.parse(message.body);
          // Live coordinates slide animation hook
          setVehicles(prev =>
            prev.map(v =>
              v.id === payload.vehicleId
                ? { ...v, currentLatitude: payload.latitude, currentLongitude: payload.longitude }
                : v
            )
          );
          // Debounced refresh to update delivery stop statuses (max once per 10s)
          if (!refreshTimer) {
            refreshTimer = setTimeout(() => {
              loadAll();
              refreshTimer = null;
            }, 10000);
          }
        });
      }
    });

    client.activate();
    return () => {
      if (refreshTimer) clearTimeout(refreshTimer);
      client.deactivate();
    };
  }, [token, loadAll]);

  useEffect(() => {
    if (token) {
      loadAll();
    }
  }, [loadAll, token]);

  async function handleLogin(e) {
    e.preventDefault();
    await run(async () => {
      const authData = await apiRequest('/auth/login', {
        method: 'POST',
        body: loginForm
      });
      localStorage.setItem('fleet-auth', JSON.stringify(authData));
      setAuth(authData);
    }, 'Welcome back!');
  }

  async function handleRegister(e) {
    e.preventDefault();
    await run(async () => {
      const authData = await apiRequest('/auth/register', {
        method: 'POST',
        body: registerForm
      });
      localStorage.setItem('fleet-auth', JSON.stringify(authData));
      setAuth(authData);
    }, 'Account created successfully!');
  }

  function handleLogout() {
    localStorage.removeItem('fleet-auth');
    setAuth(null);
    setMessage('Logged out successfully.');
  }

  if (!auth) {
    return (
      <main className="auth-shell">
        <div className="auth-container">
          <div className="auth-hero">
            <div className="hero-icon"><ShieldCheck size={32} /></div>
            <h1>Fleet Dispatcher</h1>
            <p>Professional Fleet Registry, pluggable strategy route scoring, and live STOMP WebSocket coordinate simulation.</p>
            <div className="hero-features">
              <div className="hero-feature"><CheckCircle2 size={18} /> Pluggable Strategy Optimization Matrix</div>
              <div className="hero-feature"><CheckCircle2 size={18} /> Dynamic Route Score Analysis</div>
              <div className="hero-feature"><CheckCircle2 size={18} /> STOMP Live Coordinate Simulations</div>
              <div className="hero-feature"><CheckCircle2 size={18} /> Propagation-Independent Audit Logs</div>
            </div>
          </div>
          <div className="auth-panel">
            {authMode === 'login' ? (
              <>
                <div className="auth-header">
                  <h2>Welcome back</h2>
                  <p>Sign in to your dispatcher terminal</p>
                </div>
                
                {/* 1-Click Professional Demo Quick Access Shortcuts */}
                <div className="demo-accounts-bar" style={{
                  background: 'rgba(15, 23, 42, 0.45)',
                  border: '1px solid rgba(255, 255, 255, 0.08)',
                  borderRadius: '8px',
                  padding: '12px',
                  marginBottom: '16px',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '8px'
                }}>
                  <span style={{ fontSize: '0.68rem', color: '#94a3b8', fontWeight: 800, textTransform: 'uppercase', letterSpacing: '0.06em' }}>Quick Demo Access (1-Click Fill)</span>
                  <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                    <button type="button" onClick={() => setLoginForm({ username: 'admin@fleetpro.com', password: 'admin123' })} style={{ flex: 1, padding: '6px 8px', fontSize: '0.72rem', borderRadius: '6px', background: 'rgba(16, 185, 129, 0.1)', border: '1px solid rgba(16, 185, 129, 0.2)', color: '#34d399', cursor: 'pointer', fontWeight: 800, transition: 'all 0.2s' }}>ADMIN</button>
                    <button type="button" onClick={() => setLoginForm({ username: 'dispatcher@fleetpro.com', password: 'dispatcher123' })} style={{ flex: 1, padding: '6px 8px', fontSize: '0.72rem', borderRadius: '6px', background: 'rgba(14, 165, 233, 0.1)', border: '1px solid rgba(14, 165, 233, 0.2)', color: '#38bdf8', cursor: 'pointer', fontWeight: 800, transition: 'all 0.2s' }}>DISPATCHER</button>
                    <button type="button" onClick={() => setLoginForm({ username: 'driver@fleetpro.com', password: 'operator123' })} style={{ flex: 1, padding: '6px 8px', fontSize: '0.72rem', borderRadius: '6px', background: 'rgba(99, 102, 241, 0.1)', border: '1px solid rgba(99, 102, 241, 0.2)', color: '#818cf8', cursor: 'pointer', fontWeight: 800, transition: 'all 0.2s' }}>DRIVER</button>
                  </div>
                </div>

                <form onSubmit={handleLogin} className="auth-form">
                  <div className="field-group">
                    <label>Username</label>
                    <input required placeholder="Enter username" value={loginForm.username} onChange={(e) => setLoginForm({ ...loginForm, username: e.target.value })} />
                  </div>
                  <div className="field-group">
                    <label>Password</label>
                    <div className="password-input-wrapper">
                      <input required type={showPassword ? "text" : "password"} placeholder="Enter password" value={loginForm.password} onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })} />
                      <button type="button" className="password-toggle-btn" onClick={() => setShowPassword(!showPassword)}>
                        {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                      </button>
                    </div>
                  </div>
                  <button type="submit" disabled={loading}><ShieldCheck size={18} />{loading ? 'Signing in...' : 'Sign In'}</button>
                </form>
                {message && <div className={`auth-message error`}>{message}</div>}
                <div className="auth-switch">
                  Don't have an account? <button type="button" onClick={() => { setAuthMode('register'); setMessage(''); }}>Register Dispatcher</button>
                </div>
              </>
            ) : (
              <>
                <div className="auth-header">
                  <h2>Create Account</h2>
                  <p>Register dispatcher credentials</p>
                </div>
                <form onSubmit={handleRegister} className="auth-form">
                  <div className="field-row">
                    <div className="field-group">
                      <label>Username</label>
                      <input required placeholder="Username" value={registerForm.username} onChange={(e) => setRegisterForm({ ...registerForm, username: e.target.value })} />
                    </div>
                    <div className="field-group">
                      <label>Password</label>
                      <div className="password-input-wrapper">
                        <input required type={showPassword ? "text" : "password"} placeholder="Min 6 chars" value={registerForm.password} onChange={(e) => setRegisterForm({ ...registerForm, password: e.target.value })} />
                        <button type="button" className="password-toggle-btn" onClick={() => setShowPassword(!showPassword)}>
                          {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                        </button>
                      </div>
                    </div>
                  </div>
                  <div className="field-row">
                    <div className="field-group">
                      <label>Full Name</label>
                      <input required placeholder="Name" value={registerForm.name} onChange={(e) => setRegisterForm({ ...registerForm, name: e.target.value })} />
                    </div>
                    <div className="field-group">
                      <label>Role</label>
                      <select value={registerForm.role} onChange={(e) => setRegisterForm({ ...registerForm, role: e.target.value })}>
                        <option value="DISPATCHER">Dispatcher</option>
                        <option value="ADMIN">Administrator</option>
                      </select>
                    </div>
                  </div>
                  <div className="field-group">
                    <label>Email</label>
                    <input type="email" placeholder="email@example.com" value={registerForm.email} onChange={(e) => setRegisterForm({ ...registerForm, email: e.target.value })} />
                  </div>
                  <div className="field-group">
                    <label>Contact Number</label>
                    <input required type="tel" pattern="^\+?[0-9]{10,15}$" placeholder="Phone (+1234567890)" value={registerForm.contactNumber} onChange={(e) => setRegisterForm({ ...registerForm, contactNumber: e.target.value })} title="Phone number (10-15 digits)" />
                  </div>
                  <button type="submit" disabled={loading}><UserPlus size={18} />{loading ? 'Registering...' : 'Register'}</button>
                </form>
                {message && <div className={`auth-message error`}>{message}</div>}
                <div className="auth-switch">
                  Already have an account? <button type="button" onClick={() => { setAuthMode('login'); setMessage(''); }}>Sign In</button>
                </div>
              </>
            )}
          </div>
        </div>
      </main>
    );
  }

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <Truck size={28} color="#0ea5e9" />
          <h1>Fleet Console</h1>
        </div>
        <nav>
          {tabs.map((tab) => {
            const Icon = tab.icon;
            return (
              <button key={tab.id} className={`nav-btn ${activeTab === tab.id ? 'active' : ''}`} onClick={() => setActiveTab(tab.id)}>
                <Icon size={18} />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </nav>
      </aside>

      <section className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow" style={{ color: '#94a3b8', fontSize: '0.8rem', fontWeight: 600, textTransform: 'uppercase' }}>
              {auth.roles.join(', ')} &bull; {auth.name}
            </p>
            <h2>{tabs.find((t) => t.id === activeTab)?.label}</h2>
          </div>
          <div className="topbar-actions" style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            {message && <p className="message" style={{ fontSize: '0.9rem', color: '#38bdf8', fontWeight: 600 }}>{message}</p>}
            <button onClick={loadAll} disabled={loading} title="Refresh data" style={{ display: 'flex', alignItems: 'center', gap: '8px', width: 'auto', height: '38px', borderRadius: '20px', padding: '0 16px', background: 'rgba(255, 255, 255, 0.05)', border: '1px solid var(--panel-border)', color: 'var(--text-main)', cursor: 'pointer', transition: 'all var(--transition)' }}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" className={loading ? 'spin-anim' : ''} style={{ display: 'inline-block', verticalAlign: 'middle' }}>
                <path d="M21 12a9 9 0 0 0-9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" />
                <path d="M3 3v5h5" />
                <path d="M3 12a9 9 0 0 0 9 9 9.75 9.75 0 0 0 6.74-2.74L21 16" />
                <path d="M16 16h5v5" />
              </svg>
              <span style={{ fontSize: '0.85rem', fontWeight: 700, letterSpacing: '0.02em' }}>Refresh</span>
            </button>
            <button onClick={handleLogout} title="Sign out" style={{ display: 'flex', alignItems: 'center', gap: '8px', width: 'auto', height: '38px', borderRadius: '20px', padding: '0 16px', background: 'rgba(255, 255, 255, 0.05)', border: '1px solid var(--panel-border)', color: 'var(--text-main)', cursor: 'pointer', transition: 'all var(--transition)' }}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ display: 'inline-block', verticalAlign: 'middle' }}>
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                <polyline points="16 17 21 12 16 7" />
                <line x1="21" y1="12" x2="9" y2="12" />
              </svg>
              <span style={{ fontSize: '0.85rem', fontWeight: 700, letterSpacing: '0.02em' }}>Sign Out</span>
            </button>
          </div>
        </header>

        {activeTab === 'dashboard' && (
          <div className="view-grid">
            <Dashboard data={dashboardData} />
            
            {/* Live GPS tracking radar showing Simulated trucks live */}
            <div className="panel wide" style={{ border: '1px solid rgba(14, 165, 233, 0.15)' }}>
              <GpsTrackerMap vehicles={vehicles} deliveryTasks={deliveryTasks} routes={routes} />
            </div>

            {/* Premium Recharts Analytical summaries */}
            <AnalyticsCharts vehicles={vehicles} drivers={drivers} routes={routes} deliveryTasks={deliveryTasks} />
          </div>
        )}
        
        {activeTab === 'fleet' && !isDriver && (
          <FleetView
            token={token}
            vehicles={vehicles}
            drivers={drivers}
            loadAll={loadAll}
            run={run}
            isAdmin={isAdmin}
            vSearch={vSearch}
            setVSearch={setVSearch}
            vStatus={vStatus}
            setVStatus={setVStatus}
            vPage={vPage}
            setVPage={setVPage}
            dSearch={dSearch}
            setDSearch={setDSearch}
            dStatus={dStatus}
            setDStatus={setDStatus}
            dPage={dPage}
            setDPage={setDPage}
          />
        )}
        
        {activeTab === 'deliveries' && !isDriver && (
          <DeliveriesView
            token={token}
            deliveryTasks={deliveryTasks}
            loadAll={loadAll}
            run={run}
            tSearch={tSearch}
            setTSearch={setTSearch}
            tStatus={tStatus}
            setTStatus={setTStatus}
            tPage={tPage}
            setTPage={setTPage}
          />
        )}
        
        {activeTab === 'routes' && (
          <RoutesView
            token={token}
            routes={routes}
            vehicles={vehicles}
            drivers={drivers}
            deliveryTasks={deliveryTasks}
            loadAll={loadAll}
            run={run}
            isDriver={isDriver}
            driverName={auth.name}
            rSearch={rSearch}
            setRSearch={setRSearch}
            rStatus={rStatus}
            setRStatus={setRStatus}
            rPage={rPage}
            setRPage={setRPage}
          />
        )}
      </section>
    </main>
  );
}

/* ── 1. Dashboard View ── */
const Dashboard = memo(function Dashboard({ data }) {
  if (!data) return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px', width: '100%' }}>
      <section className="metric-strip">
        {[1, 2, 3, 4].map(i => (
          <div key={i} className="metric-card skeleton-pulse" style={{ height: '94px', background: 'rgba(255,255,255,0.02)', border: '1px solid rgba(255,255,255,0.06)', borderRadius: '12px' }} />
        ))}
      </section>
      <section className="panel wide skeleton-pulse" style={{ height: '240px', background: 'rgba(255,255,255,0.02)', border: '1px solid rgba(255,255,255,0.06)', borderRadius: '12px' }} />
    </div>
  );

  return (
    <>
      <section className="metric-strip">
        <div className="metric-card">
          <div className="metric-icon-wrapper"><Truck size={24} /></div>
          <div className="metric-info"><h3>{data.totalVehicles}</h3><p>Registered Trucks</p></div>
        </div>
        <div className="metric-card">
          <div className="metric-icon-wrapper" style={{ color: '#10b981', background: 'rgba(16,185,129,0.1)' }}><CheckCircle2 size={24} /></div>
          <div className="metric-info"><h3>{data.operationalVehicles}</h3><p>Operational</p></div>
        </div>
        <div className="metric-card">
          <div className="metric-icon-wrapper" style={{ color: '#fbbf24', background: 'rgba(245,158,11,0.1)' }}><Wrench size={24} /></div>
          <div className="metric-info"><h3>{data.maintenanceVehicles}</h3><p>In Maintenance</p></div>
        </div>
        <div className="metric-card">
          <div className="metric-icon-wrapper" style={{ color: '#a78bfa', background: 'rgba(139,92,246,0.1)' }}><Users2 size={24} /></div>
          <div className="metric-info"><h3>{data.totalDrivers}</h3><p>Total Drivers</p></div>
        </div>
      </section>

      <section className="panel wide">
        <h3 style={{ marginBottom: 20 }}>Fleet Operations Summary</h3>
        <div className="fleet-dashboard-grid">
          <div className="fleet-summary-card"><span className="number">{data.availableDrivers}</span><span className="label">Available Drivers</span></div>
          <div className="fleet-summary-card"><span className="number">{data.activeDrivers}</span><span className="label">Drivers On Route</span></div>
          <div className="fleet-summary-card"><span className="number">{data.unassignedDeliveries}</span><span className="label">Pending Packages</span></div>
          <div className="fleet-summary-card"><span className="number">{data.inTransitDeliveries}</span><span className="label">In Transit</span></div>
          <div className="fleet-summary-card"><span className="number">{data.completedDeliveries}</span><span className="label">Delivered Today</span></div>
          <div className="fleet-summary-card"><span className="number">{data.activeRoutes}</span><span className="label">Active Routes</span></div>
        </div>
      </section>
    </>
  );
});

/* ── 2. Fleet Assets View ── */
function statusClass(status) {
  return 'status ' + status.toLowerCase().replace(/_/g, '-');
}

const FleetView = memo(function FleetView({
  token,
  vehicles,
  drivers,
  loadAll,
  run,
  isAdmin,
  vSearch,
  setVSearch,
  vStatus,
  setVStatus,
  vPage,
  setVPage,
  dSearch,
  setDSearch,
  dStatus,
  setDStatus,
  dPage,
  setDPage
}) {
  const [showVForm, setShowVForm] = useState(false);
  const [showDForm, setShowDForm] = useState(false);

  const [vForm, setVForm] = useState({ licensePlate: '', make: '', model: '', year: 2024, capacityKg: 1000, capacityVolumeCbm: 5, fuelType: 'DIESEL' });
  const [dForm, setDForm] = useState({ name: '', contactNumber: '', email: '', licenseNumber: '', licenseExpiry: '', shiftStart: '08:00', shiftEnd: '18:00' });

  async function createVehicle(e) {
    e.preventDefault();
    await run(async () => {
      await apiRequest('/vehicles', { method: 'POST', token, body: vForm });
      setVForm({ licensePlate: '', make: '', model: '', year: 2024, capacityKg: 1000, capacityVolumeCbm: 5, fuelType: 'DIESEL' });
      setShowVForm(false);
      await loadAll();
    }, 'Vehicle registered successfully.');
  }

  async function createDriver(e) {
    e.preventDefault();
    await run(async () => {
      const body = {
        ...dForm,
        licenseExpiry: dForm.licenseExpiry ? new Date(dForm.licenseExpiry).toISOString() : null,
        shiftStart: dForm.shiftStart + ':00',
        shiftEnd: dForm.shiftEnd + ':00'
      };
      await apiRequest('/drivers', { method: 'POST', token, body });
      setDForm({ name: '', contactNumber: '', email: '', licenseNumber: '', licenseExpiry: '', shiftStart: '08:00', shiftEnd: '18:00' });
      setShowDForm(false);
      await loadAll();
    }, 'Driver registered successfully.');
  }

  async function toggleMaintenance(vId, currentStatus) {
    const next = currentStatus === 'OPERATIONAL' ? 'IN_MAINTENANCE' : 'OPERATIONAL';
    await run(async () => {
      await apiRequest(`/vehicles/${vId}/status`, { method: 'PATCH', token, body: { status: next } });
      await loadAll();
    }, `Vehicle marked ${next.replace('_', ' ')}.`);
  }

  async function assignVehicle(dId, vId) {
    await run(async () => {
      if (vId === '') {
        await apiRequest(`/drivers/${dId}/unassign`, { method: 'PATCH', token });
      } else {
        await apiRequest(`/drivers/${dId}/assign`, { method: 'PATCH', token, body: { vehicleId: Number(vId) } });
      }
      await loadAll();
    }, 'Driver vehicle assignment updated.');
  }

  return (
    <div className="view-grid">
      <section className="panel wide">
        <div className="panel-header">
          <h3>Vehicle Registry</h3>
          <button onClick={() => setShowVForm(!showVForm)}><Plus size={16} /> {showVForm ? 'Cancel' : 'Register Vehicle'}</button>
        </div>

        {/* Searching & Filtering controls */}
        <div style={{ display: 'flex', gap: '12px', marginBottom: '20px', flexWrap: 'wrap' }}>
          <div style={{ position: 'relative', flex: 1, minWidth: '200px' }}>
            <Search size={16} style={{ position: 'absolute', left: '14px', top: '15px', color: 'var(--text-muted)' }} />
            <input placeholder="Search make, model, license plate..." value={vSearch} onChange={(e) => { setVSearch(e.target.value); setVPage(0); }} style={{ paddingLeft: '40px' }} />
          </div>
          <select value={vStatus} onChange={(e) => { setVStatus(e.target.value); setVPage(0); }} style={{ width: '180px' }}>
            <option value="">All Statuses</option>
            <option value="OPERATIONAL">Operational</option>
            <option value="IN_MAINTENANCE">In Maintenance</option>
            <option value="SCHEDULED_MAINTENANCE">Scheduled Maintenance</option>
          </select>
        </div>

        {showVForm && (
          <form onSubmit={createVehicle} className="form-grid" style={{ marginBottom: 30 }}>
            <input required placeholder="Plate (e.g. KA-01-AB-1234)" value={vForm.licensePlate} onChange={(e) => setVForm({ ...vForm, licensePlate: e.target.value })} />
            <input required placeholder="Make (e.g. Tata)" value={vForm.make} onChange={(e) => setVForm({ ...vForm, make: e.target.value })} />
            <input required placeholder="Model" value={vForm.model} onChange={(e) => setVForm({ ...vForm, model: e.target.value })} />
            <input required type="number" placeholder="Year" value={vForm.year} onChange={(e) => setVForm({ ...vForm, year: Number(e.target.value) })} />
            <input required type="number" min="1" step="any" placeholder="Weight Limit (kg)" value={vForm.capacityKg} onChange={(e) => setVForm({ ...vForm, capacityKg: Number(e.target.value) })} />
            <input type="number" min="0" step="any" placeholder="Volume Limit (m3)" value={vForm.capacityVolumeCbm} onChange={(e) => setVForm({ ...vForm, capacityVolumeCbm: Number(e.target.value) })} />
            <select value={vForm.fuelType} onChange={(e) => setVForm({ ...vForm, fuelType: e.target.value })}>
              <option value="DIESEL">Diesel</option>
              <option value="PETROL">Petrol</option>
              <option value="CNG">CNG</option>
              <option value="ELECTRIC">Electric</option>
            </select>
            <button type="submit">Submit</button>
          </form>
        )}

        <div className="fleet-cards">
          {vehicles.map((v) => (
            <div className="fleet-card" key={v.id}>
              <div className="fleet-card-header">
                <div>
                  <h4>{v.licensePlate}</h4>
                  <div className="subtitle">{v.make} {v.model} ({v.year})</div>
                </div>
                <span className={statusClass(v.maintenanceStatus)}>{v.maintenanceStatus}</span>
              </div>
              <div className="fleet-card-body">
                <div className="fleet-card-stat"><label>Weight Capacity</label><span>{v.capacityKg} kg</span></div>
                <div className="fleet-card-stat"><label>Vol Capacity</label><span>{v.capacityVolumeCbm || '-'} m3</span></div>
                <div className="fleet-card-stat"><label>Fuel Type</label><span>{v.fuelType}</span></div>
                <div className="fleet-card-stat"><label>Odometer</label><span>{v.currentOdometerKm?.toFixed(1) || 0} km</span></div>
              </div>
              <div className="fleet-card-actions">
                <button className="secondary" onClick={() => toggleMaintenance(v.id, v.maintenanceStatus)}>
                  <Wrench size={12} /> {v.maintenanceStatus === 'OPERATIONAL' ? 'Service Required' : 'Mark Restored'}
                </button>
              </div>
            </div>
          ))}
          {vehicles.length === 0 && <div className="empty-state" style={{ gridColumn: '1/-1' }}><Truck size={40} /><p>No matching vehicles found.</p></div>}
        </div>

        {/* Pagination Controls */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '20px' }}>
          <button className="secondary" disabled={vPage === 0} onClick={() => setVPage(v => v - 1)}>Prev</button>
          <button className="secondary" disabled={vehicles.length < 10} onClick={() => setVPage(v => v + 1)}>Next</button>
        </div>
      </section>

      <section className="panel wide">
        <div className="panel-header">
          <h3>Driver Manifest Registry</h3>
          <button onClick={() => setShowDForm(!showDForm)}><Plus size={16} /> {showDForm ? 'Cancel' : 'Register Driver'}</button>
        </div>

        {/* Search controls */}
        <div style={{ display: 'flex', gap: '12px', marginBottom: '20px', flexWrap: 'wrap' }}>
          <div style={{ position: 'relative', flex: 1, minWidth: '200px' }}>
            <Search size={16} style={{ position: 'absolute', left: '14px', top: '15px', color: 'var(--text-muted)' }} />
            <input placeholder="Search driver name, license, email..." value={dSearch} onChange={(e) => { setDSearch(e.target.value); setDPage(0); }} style={{ paddingLeft: '40px' }} />
          </div>
          <select value={dStatus} onChange={(e) => { setDStatus(e.target.value); setDPage(0); }} style={{ width: '180px' }}>
            <option value="">All Statuses</option>
            <option value="AVAILABLE">Available</option>
            <option value="ON_ROUTE">On Route</option>
          </select>
        </div>

        {showDForm && (
          <form onSubmit={createDriver} className="form-grid" style={{ marginBottom: 30 }}>
            <input required placeholder="Name" value={dForm.name} onChange={(e) => setDForm({ ...dForm, name: e.target.value })} />
            <input placeholder="Contact Phone" value={dForm.contactNumber} onChange={(e) => setDForm({ ...dForm, contactNumber: e.target.value })} />
            <input type="email" placeholder="Email" value={dForm.email} onChange={(e) => setDForm({ ...dForm, email: e.target.value })} />
            <input required placeholder="License Number" value={dForm.licenseNumber} onChange={(e) => setDForm({ ...dForm, licenseNumber: e.target.value })} />
            <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
              <label style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>License Expiry</label>
              <input required type="date" value={dForm.licenseExpiry} onChange={(e) => setDForm({ ...dForm, licenseExpiry: e.target.value })} />
            </div>
            <input type="time" placeholder="Shift Start" value={dForm.shiftStart} onChange={(e) => setDForm({ ...dForm, shiftStart: e.target.value })} />
            <input type="time" placeholder="Shift End" value={dForm.shiftEnd} onChange={(e) => setDForm({ ...dForm, shiftEnd: e.target.value })} />
            <button type="submit">Submit</button>
          </form>
        )}

        <div className="fleet-cards">
          {drivers.map((d) => (
            <div className="fleet-card" key={d.id}>
              <div className="fleet-card-header">
                <div>
                  <h4>{d.name}</h4>
                  <div className="subtitle">{d.licenseNumber}</div>
                </div>
                <span className={statusClass(d.status)}>{d.status}</span>
              </div>
              <div className="fleet-card-body">
                <div className="fleet-card-stat"><label>Duty Shift</label><span>{d.shiftStart || '08:00'} - {d.shiftEnd || '18:00'}</span></div>
                <div className="fleet-card-stat"><label>License Expiry</label><span style={{ color: d.licenseValid ? '#34d399' : '#f87171' }}>{new Date(d.licenseExpiry).toLocaleDateString()}</span></div>
                <div className="fleet-card-stat"><label>Phone</label><span>{d.contactNumber || '-'}</span></div>
                <div className="fleet-card-stat" style={{ alignItems: 'center' }}>
                  <label>Vehicle</label>
                  <select style={{ width: 140, padding: '4px 8px', fontSize: '0.8rem', minHeight: 28 }} value={d.assignedVehicleId || ''} onChange={(e) => assignVehicle(d.id, e.target.value)}>
                    <option value="">Unassigned</option>
                    {vehicles.filter(v => v.maintenanceStatus === 'OPERATIONAL').map(v => <option key={v.id} value={v.id}>{v.licensePlate}</option>)}
                  </select>
                </div>
              </div>
            </div>
          ))}
          {drivers.length === 0 && <div className="empty-state" style={{ gridColumn: '1/-1' }}><Users2 size={40} /><p>No matching drivers found.</p></div>}
        </div>

        {/* Pagination Controls */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '20px' }}>
          <button className="secondary" disabled={dPage === 0} onClick={() => setDPage(v => v - 1)}>Prev</button>
          <button className="secondary" disabled={drivers.length < 10} onClick={() => setDPage(v => v + 1)}>Next</button>
        </div>
      </section>
    </div>
  );
});

/* ── 3. Deliveries View ── */
const DeliveriesView = memo(function DeliveriesView({
  token,
  deliveryTasks,
  loadAll,
  run,
  tSearch,
  setTSearch,
  tStatus,
  setTStatus,
  tPage,
  setTPage
}) {
  const [showForm, setShowForm] = useState(false);
  const emptyDeliveryForm = { deliveryAddress: '', recipientName: '', recipientPhone: '', latitude: '', longitude: '', packageWeightKg: '', packageVolumeCbm: '', timeWindowStart: '', timeWindowEnd: '', notes: '' };
  const [form, setForm] = useState(emptyDeliveryForm);

  async function handleSubmit(e) {
    e.preventDefault();
    await run(async () => {
      if (form.timeWindowStart && form.timeWindowEnd && new Date(form.timeWindowStart) >= new Date(form.timeWindowEnd)) {
        throw new Error('Window Start must be before Window End.');
      }
      const body = {
        ...form,
        latitude: Number(form.latitude),
        longitude: Number(form.longitude),
        packageWeightKg: form.packageWeightKg ? Number(form.packageWeightKg) : null,
        packageVolumeCbm: form.packageVolumeCbm ? Number(form.packageVolumeCbm) : null,
        timeWindowStart: form.timeWindowStart ? new Date(form.timeWindowStart).toISOString() : null,
        timeWindowEnd: form.timeWindowEnd ? new Date(form.timeWindowEnd).toISOString() : null
      };
      await apiRequest('/deliveries', { method: 'POST', token, body });
      setForm(emptyDeliveryForm);
      setShowForm(false);
      await loadAll();
    }, 'Delivery Task added successfully.');
  }

  async function updateStatus(taskId, nextStatus) {
    await run(async () => {
      await apiRequest(`/deliveries/${taskId}/status`, { method: 'PATCH', token, body: { status: nextStatus } });
      await loadAll();
    }, `Status updated to ${nextStatus}.`);
  }

  function getCandidateTransitions(currentStatus) {
    switch (currentStatus) {
      case 'UNASSIGNED': return ['DISPATCHED'];
      case 'DISPATCHED': return ['IN_TRANSIT'];
      case 'IN_TRANSIT': return ['DELIVERED', 'FAILED'];
      default: return [];
    }
  }

  return (
    <div className="view-grid">
      <section className="panel wide">
        <div className="panel-header">
          <h3>Delivery Registry Stop Orders</h3>
          <button onClick={() => setShowForm(!showForm)}><Plus size={16} /> {showForm ? 'Cancel' : 'New Outbound Stop'}</button>
        </div>

        {/* Searching & Filtering controls */}
        <div style={{ display: 'flex', gap: '12px', marginBottom: '20px', flexWrap: 'wrap' }}>
          <div style={{ position: 'relative', flex: 1, minWidth: '200px' }}>
            <Search size={16} style={{ position: 'absolute', left: '14px', top: '15px', color: 'var(--text-muted)' }} />
            <input placeholder="Search destination, recipient..." value={tSearch} onChange={(e) => { setTSearch(e.target.value); setTPage(0); }} style={{ paddingLeft: '40px' }} />
          </div>
          <select value={tStatus} onChange={(e) => { setTStatus(e.target.value); setTPage(0); }} style={{ width: '180px' }}>
            <option value="">All Statuses</option>
            <option value="UNASSIGNED">Unassigned</option>
            <option value="DISPATCHED">Dispatched</option>
            <option value="IN_TRANSIT">In Transit</option>
            <option value="DELIVERED">Delivered</option>
            <option value="FAILED">Failed</option>
          </select>
        </div>

        {showForm && (
          <form onSubmit={handleSubmit} className="form-grid" style={{ marginBottom: 30 }}>
            <input required placeholder="Delivery Address" value={form.deliveryAddress} onChange={(e) => setForm({ ...form, deliveryAddress: e.target.value })} />
            <input placeholder="Recipient Name" value={form.recipientName} onChange={(e) => setForm({ ...form, recipientName: e.target.value })} />
            <input placeholder="Recipient Phone" value={form.recipientPhone} onChange={(e) => setForm({ ...form, recipientPhone: e.target.value })} />
            <input required type="number" step="any" placeholder="Latitude (e.g. 12.9716)" value={form.latitude} onChange={(e) => setForm({ ...form, latitude: e.target.value })} />
            <input required type="number" step="any" placeholder="Longitude (e.g. 77.5946)" value={form.longitude} onChange={(e) => setForm({ ...form, longitude: e.target.value })} />
            <input type="number" min="0" step="any" placeholder="Weight (kg)" value={form.packageWeightKg} onChange={(e) => setForm({ ...form, packageWeightKg: e.target.value })} />
            <input type="number" min="0" step="any" placeholder="Volume (m3)" value={form.packageVolumeCbm} onChange={(e) => setForm({ ...form, packageVolumeCbm: e.target.value })} />
            <div className="field-group">
              <label>Window Start</label>
              <input type="datetime-local" value={form.timeWindowStart} onChange={(e) => setForm({ ...form, timeWindowStart: e.target.value })} />
            </div>
            <div className="field-group">
              <label>Window End</label>
              <input type="datetime-local" value={form.timeWindowEnd} onChange={(e) => setForm({ ...form, timeWindowEnd: e.target.value })} />
            </div>
            <input placeholder="Notes / Instruction" value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} />
            <button type="submit">Create Stop</button>
          </form>
        )}

        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Destination Address</th>
                <th>Recipient</th>
                <th>Coordinates</th>
                <th>Weight (kg)</th>
                <th>Time Window</th>
                <th>Status</th>
                <th>Route ID</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {deliveryTasks.map((t) => (
                <tr key={t.id}>
                  <td>{t.id}</td>
                  <td>{t.deliveryAddress}</td>
                  <td>
                    <strong>{t.recipientName || 'Unassigned'}</strong>
                    {t.recipientPhone && <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{t.recipientPhone}</div>}
                  </td>
                  <td><span className="gps-badge"><MapPin size={12} /> {t.latitude?.toFixed(4)}, {t.longitude?.toFixed(4)}</span></td>
                  <td>{t.packageWeightKg || '-'}</td>
                  <td>{formatTimeWindow(t.timeWindowStart, t.timeWindowEnd)}</td>
                  <td><span className={statusClass(t.deliveryStatus)}>{t.deliveryStatus}</span></td>
                  <td>{t.routeName || '-'}</td>
                  <td>
                    <div style={{ display: 'flex', gap: 6 }}>
                      {getCandidateTransitions(t.deliveryStatus).map(st => (
                        <button key={st} className="secondary" style={{ fontSize: '0.75rem', padding: '6px 12px', minHeight: 28 }} onClick={() => updateStatus(t.id, st)}>
                          {st}
                        </button>
                      ))}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {deliveryTasks.length === 0 && <div className="empty-state"><MapPin size={40} /><p>No delivery tasks found.</p></div>}

        {/* Pagination */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '20px' }}>
          <button className="secondary" disabled={tPage === 0} onClick={() => setTPage(v => v - 1)}>Prev</button>
          <button className="secondary" disabled={deliveryTasks.length < 10} onClick={() => setTPage(v => v + 1)}>Next</button>
        </div>
      </section>
    </div>
  );
});

/* ── 4. Routes View ── */
const RoutesView = memo(function RoutesView({
  token,
  routes,
  vehicles,
  drivers,
  deliveryTasks,
  loadAll,
  run,
  isDriver,
  driverName,
  rSearch,
  setRSearch,
  rStatus,
  setRStatus,
  rPage,
  setRPage
}) {
  const [showPlanner, setShowPlanner] = useState(false);
  const [selectedTasks, setSelectedTasks] = useState([]);
  const [vId, setVId] = useState('');
  const [dId, setDId] = useState('');
  const [plannedDeparture, setPlannedDeparture] = useState(() => toDateTimeLocalValue(new Date(Date.now() + 30 * 60000)));
  const [expandedRoute, setExpandedRoute] = useState(null);

  // Filter routes if role is DRIVER to only show their assigned routes!
  const filteredRoutes = useMemo(() => {
    if (isDriver) {
      return routes.filter(r => r.driverName === driverName);
    }
    return routes;
  }, [routes, isDriver, driverName]);

  const unassignedTasks = useMemo(() => deliveryTasks.filter(t => t.deliveryStatus === 'UNASSIGNED' && !t.routeId), [deliveryTasks]);
  const selectedTaskDetails = useMemo(() => unassignedTasks.filter(t => selectedTasks.includes(t.id)), [unassignedTasks, selectedTasks]);
  const selectedVehicle = useMemo(() => vehicles.find(v => String(v.id) === String(vId)), [vehicles, vId]);
  const selectedDriver = useMemo(() => drivers.find(d => String(d.id) === String(dId)), [drivers, dId]);
  const selectedLoad = useMemo(() => selectedTaskDetails.reduce((totals, task) => ({
    weightKg: totals.weightKg + Number(task.packageWeightKg || 0),
    volumeCbm: totals.volumeCbm + Number(task.packageVolumeCbm || 0)
  }), { weightKg: 0, volumeCbm: 0 }), [selectedTaskDetails]);

  const routeValidationErrors = useMemo(() => {
    const errors = [];
    if (selectedVehicle) {
      if (selectedVehicle.maintenanceStatus !== 'OPERATIONAL') {
        errors.push('Selected vehicle is not operational.');
      }
      if (selectedLoad.weightKg > Number(selectedVehicle.capacityKg || 0)) {
        errors.push(`Load weight ${selectedLoad.weightKg.toFixed(1)} kg exceeds vehicle capacity ${Number(selectedVehicle.capacityKg || 0).toFixed(1)} kg.`);
      }
      if (selectedLoad.volumeCbm > 0 && selectedVehicle.capacityVolumeCbm == null) {
        errors.push('Selected load has volume, but vehicle volume capacity is missing.');
      }
      if (selectedVehicle.capacityVolumeCbm != null && selectedLoad.volumeCbm > Number(selectedVehicle.capacityVolumeCbm)) {
        errors.push(`Load volume ${selectedLoad.volumeCbm.toFixed(1)} m3 exceeds vehicle capacity ${Number(selectedVehicle.capacityVolumeCbm).toFixed(1)} m3.`);
      }
    }
    if (selectedDriver) {
      if (selectedDriver.status !== 'AVAILABLE') {
        errors.push('Selected driver is not available.');
      }
      if (!selectedDriver.licenseValid) {
        errors.push('Selected driver license is expired.');
      }
      if (selectedVehicle && selectedDriver.assignedVehicleId && selectedDriver.assignedVehicleId !== selectedVehicle.id) {
        errors.push(`Selected driver is assigned to ${selectedDriver.assignedVehiclePlate}, not ${selectedVehicle.licensePlate}.`);
      }
    }
    if (plannedDeparture) {
      const departureDate = new Date(plannedDeparture);
      selectedTaskDetails.forEach(task => {
        if (task.timeWindowEnd && departureDate > new Date(task.timeWindowEnd)) {
          errors.push(`Stop ${task.id} already missed its delivery window.`);
        }
      });
    }
    return errors;
  }, [plannedDeparture, selectedDriver, selectedLoad, selectedTaskDetails, selectedVehicle]);

  const canOptimize = selectedTasks.length > 0 && vId && dId && plannedDeparture && routeValidationErrors.length === 0;

  function toggleTask(id) {
    setSelectedTasks(prev => prev.includes(id) ? prev.filter(t => t !== id) : [...prev, id]);
  }

  async function optimizeRoute(e) {
    e.preventDefault();
    await run(async () => {
      if (!canOptimize) {
        throw new Error(routeValidationErrors[0] || 'Please complete the route plan.');
      }
      await apiRequest('/routes/optimize', {
        method: 'POST',
        token,
        body: {
          deliveryTaskIds: selectedTasks,
          vehicleId: Number(vId),
          driverId: Number(dId),
          plannedDepartureTime: new Date(plannedDeparture).toISOString()
        }
      });
      setSelectedTasks([]);
      setVId('');
      setDId('');
      setPlannedDeparture(toDateTimeLocalValue(new Date(Date.now() + 30 * 60000)));
      setShowPlanner(false);
      await loadAll();
    }, 'Route optimized successfully!');
  }

  async function dispatchRoute(rId) {
    await run(async () => {
      await apiRequest(`/routes/${rId}/dispatch`, { method: 'POST', token });
      await loadAll();
    }, 'Route dispatched successfully.');
  }

  async function completeRoute(rId) {
    await run(async () => {
      await apiRequest(`/routes/${rId}/complete`, { method: 'POST', token });
      await loadAll();
    }, 'Route completed & odometer updated.');
  }

  // Update status for drivers
  async function updateStopStatus(taskId, nextStatus) {
    await run(async () => {
      await apiRequest(`/deliveries/${taskId}/status`, {
        method: 'PATCH',
        token,
        body: { status: nextStatus }
      });
      await loadAll();
    }, `Stop marked ${nextStatus}`);
  }

  return (
    <div className="view-grid">
      <section className="panel wide">
        <div className="panel-header">
          <h3>Optimized Delivery Routes</h3>
          {!isDriver && (
            <button onClick={() => setShowPlanner(!showPlanner)}><Navigation size={16} /> {showPlanner ? 'Cancel' : 'Plan Optimized Route'}</button>
          )}
        </div>

        {/* Searching & Filtering controls */}
        {!isDriver && (
          <div style={{ display: 'flex', gap: '12px', marginBottom: '20px', flexWrap: 'wrap' }}>
            <div style={{ position: 'relative', flex: 1, minWidth: '200px' }}>
              <Search size={16} style={{ position: 'absolute', left: '14px', top: '15px', color: 'var(--text-muted)' }} />
              <input placeholder="Search route name, driver, vehicle..." value={rSearch} onChange={(e) => { setRSearch(e.target.value); setRPage(0); }} style={{ paddingLeft: '40px' }} />
            </div>
            <select value={rStatus} onChange={(e) => { setRStatus(e.target.value); setRPage(0); }} style={{ width: '180px' }}>
              <option value="">All Statuses</option>
              <option value="PLANNED">Planned</option>
              <option value="ACTIVE">Active</option>
              <option value="COMPLETED">Completed</option>
            </select>
          </div>
        )}

        {showPlanner && !isDriver && (
          <div style={{ marginBottom: 30 }}>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: 16 }}>Select unassigned delivery stops, then choose a compatible truck, driver, and departure time. The planner checks capacity, driver license, assigned vehicle, shift, and delivery windows before optimizing.</p>
            {unassignedTasks.length > 0 ? (
              <div className="stop-list" style={{ marginBottom: 20 }}>
                {unassignedTasks.map(t => (
                  <label key={t.id} className="stop-item" style={{ cursor: 'pointer' }}>
                    <input type="checkbox" checked={selectedTasks.includes(t.id)} onChange={() => toggleTask(t.id)} style={{ width: 18, height: 18, minHeight: 18 }} />
                    <div className="stop-info">
                      <h5>{t.deliveryAddress}</h5>
                      <p>{t.recipientName || 'Unassigned'} - {t.packageWeightKg || 0} kg - {formatTimeWindow(t.timeWindowStart, t.timeWindowEnd)}</p>
                    </div>
                    <span className="gps-badge"><MapPin size={12} /> {t.latitude.toFixed(4)}, {t.longitude.toFixed(4)}</span>
                  </label>
                ))}
              </div>
            ) : (
              <div className="empty-state" style={{ padding: 24 }}><MapPin size={32} /><p>No unassigned delivery stops available.</p></div>
            )}

            {selectedTasks.length > 0 && (
              <>
                <div className="planning-summary">
                  <div><strong>{selectedTasks.length}</strong><span>Stops</span></div>
                  <div><strong>{selectedLoad.weightKg.toFixed(1)} kg</strong><span>Total Weight</span></div>
                  <div><strong>{selectedLoad.volumeCbm.toFixed(1)} m3</strong><span>Total Volume</span></div>
                </div>
                {routeValidationErrors.length > 0 && (
                  <div className="warning-note">
                    {routeValidationErrors.map(error => <p key={error}>{error}</p>)}
                  </div>
                )}
                <form onSubmit={optimizeRoute} className="form-grid route-form">
                  <div className="field-group">
                    <label>Select Operational Vehicle</label>
                    <select required value={vId} onChange={(e) => setVId(e.target.value)}>
                      <option value="">Choose Vehicle</option>
                      {vehicles.filter(v => v.maintenanceStatus === 'OPERATIONAL').map(v => (
                        <option key={v.id} value={v.id}>{v.licensePlate} ({v.capacityKg} kg / {v.capacityVolumeCbm || '-'} m3)</option>
                      ))}
                    </select>
                  </div>
                  <div className="field-group">
                    <label>Select Available Driver</label>
                    <select required value={dId} onChange={(e) => setDId(e.target.value)}>
                      <option value="">Choose Driver</option>
                      {drivers.filter(d => d.status === 'AVAILABLE' && d.licenseValid).map(d => (
                        <option key={d.id} value={d.id}>{d.name}{d.assignedVehiclePlate ? ` - ${d.assignedVehiclePlate}` : ''}</option>
                      ))}
                    </select>
                  </div>
                  <div className="field-group">
                    <label>Planned Departure</label>
                    <input required type="datetime-local" value={plannedDeparture} onChange={(e) => setPlannedDeparture(e.target.value)} />
                  </div>
                  <button type="submit" disabled={!canOptimize} style={{ height: 48 }}><Navigation size={14} /> Calculate Route</button>
                </form>
              </>
            )}
          </div>
        )}

        <div className="fleet-cards">
          {filteredRoutes.map(r => (
            <div className="fleet-card" key={r.id}>
              <div className="fleet-card-header">
                <div>
                  <h4 style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    {r.routeName}
                    {r.routeScore && (
                      <span style={{
                        fontSize: '0.7rem',
                        background: r.routeScore >= 80 ? 'rgba(16,185,129,0.1)' : 'rgba(245,158,11,0.1)',
                        color: r.routeScore >= 80 ? '#34d399' : '#fbbf24',
                        padding: '2px 8px',
                        borderRadius: '4px',
                        border: '1px solid currentColor'
                      }}>
                        Score: {r.routeScore}
                      </span>
                    )}
                  </h4>
                  <div className="subtitle">{r.vehiclePlate} &bull; {r.driverName}</div>
                </div>
                <span className={statusClass(r.status)}>{r.status}</span>
              </div>

              {r.totalDistanceKm && (
                <div className="route-metrics">
                  <div className="route-metric"><span className="value">{r.totalDistanceKm}</span><span className="label">Dist (km)</span></div>
                  <div className="route-metric"><span className="value">{r.estimatedDurationMinutes}</span><span className="label">Dur (min)</span></div>
                  <div className="route-metric"><span className="value">{r.totalFuelEstimateLiters}</span><span className="label">Fuel (L)</span></div>
                  <div className="route-metric"><span className="value">{r.stopCount}</span><span className="label">Stops</span></div>
                </div>
              )}

              <div className="fleet-card-actions">
                {r.status === 'PLANNED' && !isDriver && (
                  <button onClick={() => dispatchRoute(r.id)}><Navigation size={14} /> Dispatch</button>
                )}
                {r.status === 'ACTIVE' && !isDriver && (
                  <button onClick={() => completeRoute(r.id)} className="secondary"><CheckCircle2 size={14} /> Complete Route</button>
                )}
                <button className="secondary" onClick={() => setExpandedRoute(expandedRoute === r.id ? null : r.id)}>
                  {expandedRoute === r.id ? <EyeOff size={14} /> : <Eye size={14} />} {expandedRoute === r.id ? 'Hide stops' : 'Manifest list'}
                </button>
              </div>

              {expandedRoute === r.id && r.deliveryStops && (
                <div className="stop-list" style={{ marginTop: 12 }}>
                  {r.deliveryStops.map((stop, idx) => (
                    <div className="stop-item" key={stop.id}>
                      <div className="stop-number">{idx + 1}</div>
                      <div className="stop-info">
                        <h5>{stop.deliveryAddress}</h5>
                        <p>{stop.recipientName} &bull; {stop.packageWeightKg || 0} kg</p>
                      </div>
                      <span className={statusClass(stop.deliveryStatus)}>{stop.deliveryStatus}</span>
                      
                      {/* Driver actions directly in the stops list */}
                      {isDriver && r.status === 'ACTIVE' && (
                        <div style={{ display: 'flex', gap: '6px', marginLeft: 'auto' }}>
                          {stop.deliveryStatus === 'IN_TRANSIT' && (
                            <>
                              <button className="secondary" style={{ fontSize: '0.7rem', padding: '4px 8px', minHeight: '24px' }} onClick={() => updateStopStatus(stop.id, 'DELIVERED')}>Deliver</button>
                              <button className="danger" style={{ fontSize: '0.7rem', padding: '4px 8px', minHeight: '24px' }} onClick={() => updateStopStatus(stop.id, 'FAILED')}>Fail</button>
                            </>
                          )}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}
          {filteredRoutes.length === 0 && <div className="empty-state" style={{ gridColumn: '1/-1' }}><Navigation size={40} /><p>No routes optimized yet.</p></div>}
        </div>

        {/* Pagination */}
        {!isDriver && (
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '20px' }}>
            <button className="secondary" disabled={rPage === 0} onClick={() => setRPage(v => v - 1)}>Prev</button>
            <button className="secondary" disabled={routes.length < 10} onClick={() => setRPage(v => v + 1)}>Next</button>
          </div>
        )}
      </section>
    </div>
  );
});

export default App;
