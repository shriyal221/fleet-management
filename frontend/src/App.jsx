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
  UserPlus
} from 'lucide-react';
import { useEffect, useMemo, useState, memo } from 'react';
import { apiRequest } from './api.js';

const tabs = [
  { id: 'dashboard', label: 'Dashboard', icon: Truck },
  { id: 'fleet', label: 'Fleet Registry', icon: Users2 },
  { id: 'deliveries', label: 'Deliveries', icon: MapPin },
  { id: 'routes', label: 'Route Planner', icon: Navigation }
];

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

  const [loginForm, setLoginForm] = useState({ username: '', password: '' });
  const [registerForm, setRegisterForm] = useState({ username: '', password: '', role: 'DISPATCHER', name: '', email: '', contactNumber: '' });
  const [authMode, setAuthMode] = useState('login');

  const token = auth?.token;

  useEffect(() => {
    if (token) {
      loadAll();
    }
  }, [token]);

  async function run(action, successMsg) {
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
  }

  async function loadAll() {
    await run(async () => {
      const [nextVehicles, nextDrivers, nextDeliveries, nextRoutes, nextDashboard] = await Promise.all([
        apiRequest('/vehicles', { token }),
        apiRequest('/drivers', { token }),
        apiRequest('/deliveries', { token }),
        apiRequest('/routes', { token }),
        apiRequest('/routes/dashboard', { token })
      ]);
      setVehicles(nextVehicles);
      setDrivers(nextDrivers);
      setDeliveryTasks(nextDeliveries);
      setRoutes(nextRoutes);
      setDashboardData(nextDashboard);
    }, null);
  }

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
            <p>Enterprise Fleet Telematics, Shift Scheduling, and TSP Route Optimization Engine.</p>
            <div className="hero-features">
              <div className="hero-feature"><CheckCircle2 size={18} /> OSRM Route Optimization Matrix</div>
              <div className="hero-feature"><CheckCircle2 size={18} /> Driver & Shift Scheduling</div>
              <div className="hero-feature"><CheckCircle2 size={18} /> Package Delivery State Machine</div>
              <div className="hero-feature"><CheckCircle2 size={18} /> Consolidated Delivery Manifests</div>
            </div>
          </div>
          <div className="auth-panel">
            {authMode === 'login' ? (
              <>
                <div className="auth-header">
                  <h2>Welcome back</h2>
                  <p>Sign in to your dispatcher terminal</p>
                </div>
                <form onSubmit={handleLogin} className="auth-form">
                  <div className="field-group">
                    <label>Username</label>
                    <input required placeholder="Enter username" value={loginForm.username} onChange={(e) => setLoginForm({ ...loginForm, username: e.target.value })} />
                  </div>
                  <div className="field-group">
                    <label>Password</label>
                    <input required type="password" placeholder="Enter password" value={loginForm.password} onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })} />
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
                  <p>Register a standalone dispatcher credentials</p>
                </div>
                <form onSubmit={handleRegister} className="auth-form">
                  <div className="field-row">
                    <div className="field-group">
                      <label>Username</label>
                      <input required placeholder="Username" value={registerForm.username} onChange={(e) => setRegisterForm({ ...registerForm, username: e.target.value })} />
                    </div>
                    <div className="field-group">
                      <label>Password</label>
                      <input required type="password" placeholder="Min 6 chars" value={registerForm.password} onChange={(e) => setRegisterForm({ ...registerForm, password: e.target.value })} />
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
          <div className="topbar-actions">
            {message && <p className="message" style={{ fontSize: '0.9rem', color: '#38bdf8', fontWeight: 600 }}>{message}</p>}
            <button onClick={loadAll} disabled={loading} title="Refresh data">
              <RefreshCw size={18} className={loading ? 'spin-anim' : ''} />
            </button>
            <button onClick={handleLogout} title="Sign out">
              <LogOut size={18} />
            </button>
          </div>
        </header>

        {activeTab === 'dashboard' && <Dashboard data={dashboardData} />}
        {activeTab === 'fleet' && (
          <FleetView token={token} vehicles={vehicles} drivers={drivers} loadAll={loadAll} run={run} />
        )}
        {activeTab === 'deliveries' && (
          <DeliveriesView token={token} deliveryTasks={deliveryTasks} loadAll={loadAll} run={run} />
        )}
        {activeTab === 'routes' && (
          <RoutesView token={token} routes={routes} vehicles={vehicles} drivers={drivers} deliveryTasks={deliveryTasks} loadAll={loadAll} run={run} />
        )}
      </section>
    </main>
  );
}

/* ── 1. Dashboard View ── */
const Dashboard = memo(function Dashboard({ data }) {
  if (!data) return <div className="empty-state"><RefreshCw size={40} /><p>Loading dashboard metrics...</p></div>;

  return (
    <div className="view-grid">
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
    </div>
  );
});

/* ── 2. Fleet Assets View ── */
function statusClass(status) {
  return 'status ' + status.toLowerCase().replace(/_/g, '-');
}

const FleetView = memo(function FleetView({ token, vehicles, drivers, loadAll, run }) {
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
    }, `Vehicle is now marked ${next.replace('_', ' ')}.`);
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
          <h3>Vehicle Registry ({vehicles.length})</h3>
          <button onClick={() => setShowVForm(!showVForm)}><Plus size={16} /> {showVForm ? 'Cancel' : 'Register Vehicle'}</button>
        </div>

        {showVForm && (
          <form onSubmit={createVehicle} className="form-grid" style={{ marginBottom: 30 }}>
            <input required placeholder="Plate (e.g. KA-01-AB-1234)" value={vForm.licensePlate} onChange={(e) => setVForm({ ...vForm, licensePlate: e.target.value })} />
            <input required placeholder="Make (e.g. Tata)" value={vForm.make} onChange={(e) => setVForm({ ...vForm, make: e.target.value })} />
            <input required placeholder="Model" value={vForm.model} onChange={(e) => setVForm({ ...vForm, model: e.target.value })} />
            <input required type="number" placeholder="Year" value={vForm.year} onChange={(e) => setVForm({ ...vForm, year: Number(e.target.value) })} />
            <input required type="number" placeholder="Weight Limit (kg)" value={vForm.capacityKg} onChange={(e) => setVForm({ ...vForm, capacityKg: Number(e.target.value) })} />
            <input type="number" placeholder="Volume Limit (m³)" value={vForm.capacityVolumeCbm} onChange={(e) => setVForm({ ...vForm, capacityVolumeCbm: Number(e.target.value) })} />
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
                <div className="fleet-card-stat"><label>Vol Capacity</label><span>{v.capacityVolumeCbm || '-'} m³</span></div>
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
          {vehicles.length === 0 && <div className="empty-state" style={{ gridColumn: '1/-1' }}><Truck size={40} /><p>No vehicles registered in fleet.</p></div>}
        </div>
      </section>

      <section className="panel wide">
        <div className="panel-header">
          <h3>Driver Manifest Registry ({drivers.length})</h3>
          <button onClick={() => setShowDForm(!showDForm)}><Plus size={16} /> {showDForm ? 'Cancel' : 'Register Driver'}</button>
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
          {drivers.length === 0 && <div className="empty-state" style={{ gridColumn: '1/-1' }}><Users2 size={40} /><p>No drivers profile saved.</p></div>}
        </div>
      </section>
    </div>
  );
});

/* ── 3. Deliveries View ── */
const DeliveriesView = memo(function DeliveriesView({ token, deliveryTasks, loadAll, run }) {
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ deliveryAddress: '', recipientName: '', recipientPhone: '', latitude: '', longitude: '', packageWeightKg: '', packageVolumeCbm: '', notes: '' });

  async function handleSubmit(e) {
    e.preventDefault();
    await run(async () => {
      const body = {
        ...form,
        latitude: Number(form.latitude),
        longitude: Number(form.longitude),
        packageWeightKg: form.packageWeightKg ? Number(form.packageWeightKg) : null,
        packageVolumeCbm: form.packageVolumeCbm ? Number(form.packageVolumeCbm) : null
      };
      await apiRequest('/deliveries', { method: 'POST', token, body });
      setForm({ deliveryAddress: '', recipientName: '', recipientPhone: '', latitude: '', longitude: '', packageWeightKg: '', packageVolumeCbm: '', notes: '' });
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

        {showForm && (
          <form onSubmit={handleSubmit} className="form-grid" style={{ marginBottom: 30 }}>
            <input required placeholder="Delivery Address" value={form.deliveryAddress} onChange={(e) => setForm({ ...form, deliveryAddress: e.target.value })} />
            <input placeholder="Recipient Name" value={form.recipientName} onChange={(e) => setForm({ ...form, recipientName: e.target.value })} />
            <input placeholder="Recipient Phone" value={form.recipientPhone} onChange={(e) => setForm({ ...form, recipientPhone: e.target.value })} />
            <input required type="number" step="any" placeholder="Latitude (e.g. 12.9716)" value={form.latitude} onChange={(e) => setForm({ ...form, latitude: e.target.value })} />
            <input required type="number" step="any" placeholder="Longitude (e.g. 77.5946)" value={form.longitude} onChange={(e) => setForm({ ...form, longitude: e.target.value })} />
            <input type="number" step="any" placeholder="Weight (kg)" value={form.packageWeightKg} onChange={(e) => setForm({ ...form, packageWeightKg: e.target.value })} />
            <input type="number" step="any" placeholder="Volume (m³)" value={form.packageVolumeCbm} onChange={(e) => setForm({ ...form, packageVolumeCbm: e.target.value })} />
            <input placeholder="Notes / Instruction" value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} />
            <button type="submit">Create Stop</button>
          </form>
        )}

        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Destination Address</th>
              <th>Recipient</th>
              <th>Coordinates</th>
              <th>Weight (kg)</th>
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
        {deliveryTasks.length === 0 && <div className="empty-state"><MapPin size={40} /><p>No delivery tasks found.</p></div>}
      </section>
    </div>
  );
});

/* ── 4. Routes View ── */
const RoutesView = memo(function RoutesView({ token, routes, vehicles, drivers, deliveryTasks, loadAll, run }) {
  const [showPlanner, setShowPlanner] = useState(false);
  const [selectedTasks, setSelectedTasks] = useState([]);
  const [vId, setVId] = useState('');
  const [dId, setDId] = useState('');
  const [expandedRoute, setExpandedRoute] = useState(null);

  const unassignedTasks = useMemo(() => deliveryTasks.filter(t => t.deliveryStatus === 'UNASSIGNED' && !t.routeId), [deliveryTasks]);

  function toggleTask(id) {
    setSelectedTasks(prev => prev.includes(id) ? prev.filter(t => t !== id) : [...prev, id]);
  }

  async function optimizeRoute(e) {
    e.preventDefault();
    await run(async () => {
      await apiRequest('/routes/optimize', {
        method: 'POST',
        token,
        body: {
          deliveryTaskIds: selectedTasks,
          vehicleId: Number(vId),
          driverId: Number(dId)
        }
      });
      setSelectedTasks([]);
      setVId('');
      setDId('');
      setShowPlanner(false);
      await loadAll();
    }, 'Route optimized successfully!');
  }

  async function dispatchRoute(rId) {
    await run(async () => {
      await apiRequest(`/routes/${rId}/dispatch`, { method: 'POST', token });
      await loadAll();
    }, 'Route dispatched & drivers assigned.');
  }

  async function completeRoute(rId) {
    await run(async () => {
      await apiRequest(`/routes/${rId}/complete`, { method: 'POST', token });
      await loadAll();
    }, 'Route completed & logs successfully saved.');
  }

  return (
    <div className="view-grid">
      <section className="panel wide">
        <div className="panel-header">
          <h3>Active Routes ({routes.length})</h3>
          <button onClick={() => setShowPlanner(!showPlanner)}><Navigation size={16} /> {showPlanner ? 'Cancel' : 'Plan Optimized Route'}</button>
        </div>

        {showPlanner && (
          <div style={{ marginBottom: 30 }}>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: 16 }}>Select unassigned delivery stops, then choose a driver and truck to construct a TSP optimized sequence.</p>
            {unassignedTasks.length > 0 ? (
              <div className="stop-list" style={{ marginBottom: 20 }}>
                {unassignedTasks.map(t => (
                  <label key={t.id} className="stop-item" style={{ cursor: 'pointer' }}>
                    <input type="checkbox" checked={selectedTasks.includes(t.id)} onChange={() => toggleTask(t.id)} style={{ width: 18, height: 18, minHeight: 18 }} />
                    <div className="stop-info">
                      <h5>{t.deliveryAddress}</h5>
                      <p>{t.recipientName || 'Unassigned'} • {t.packageWeightKg || 0} kg</p>
                    </div>
                    <span className="gps-badge"><MapPin size={12} /> {t.latitude.toFixed(4)}, {t.longitude.toFixed(4)}</span>
                  </label>
                ))}
              </div>
            ) : (
              <div className="empty-state" style={{ padding: 24 }}><MapPin size={32} /><p>No unassigned delivery stops available.</p></div>
            )}

            {selectedTasks.length > 0 && (
              <form onSubmit={optimizeRoute} className="form-grid" style={{ gridTemplateColumns: '1fr 1fr auto', alignItems: 'end' }}>
                <div className="field-group">
                  <label>Select Operational Vehicle</label>
                  <select required value={vId} onChange={(e) => setVId(e.target.value)}>
                    <option value="">Choose Vehicle</option>
                    {vehicles.filter(v => v.maintenanceStatus === 'OPERATIONAL').map(v => (
                      <option key={v.id} value={v.id}>{v.licensePlate} ({v.make} {v.model})</option>
                    ))}
                  </select>
                </div>
                <div className="field-group">
                  <label>Select Available Driver</label>
                  <select required value={dId} onChange={(e) => setDId(e.target.value)}>
                    <option value="">Choose Driver</option>
                    {drivers.filter(d => d.status === 'AVAILABLE').map(d => (
                      <option key={d.id} value={d.id}>{d.name}</option>
                    ))}
                  </select>
                </div>
                <button type="submit" style={{ height: 48 }}><Navigation size={14} /> Calculate TSP Route</button>
              </form>
            )}
          </div>
        )}

        <div className="fleet-cards">
          {routes.map(r => (
            <div className="fleet-card" key={r.id}>
              <div className="fleet-card-header">
                <div>
                  <h4>{r.routeName}</h4>
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
                {r.status === 'PLANNED' && (
                  <button onClick={() => dispatchRoute(r.id)}><Navigation size={14} /> Dispatch</button>
                )}
                {r.status === 'ACTIVE' && (
                  <button onClick={() => completeRoute(r.id)} className="secondary"><CheckCircle2 size={14} /> Mark Completed</button>
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
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}
          {routes.length === 0 && <div className="empty-state" style={{ gridColumn: '1/-1' }}><Navigation size={40} /><p>No planned route optimized yet.</p></div>}
        </div>
      </section>
    </div>
  );
});

export default App;
