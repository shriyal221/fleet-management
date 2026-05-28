import React from 'react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Cell,
  PieChart,
  Pie,
  Legend,
  AreaChart,
  Area
} from 'recharts';

export function AnalyticsCharts({ vehicles, drivers, routes, deliveryTasks }) {
  // 1. Vehicle Maintenance status distribution
  const maintData = [
    { name: 'Operational', value: vehicles.filter(v => v.maintenanceStatus === 'OPERATIONAL').length, color: '#10b981' },
    { name: 'In Maintenance', value: vehicles.filter(v => v.maintenanceStatus === 'IN_MAINTENANCE').length, color: '#ef4444' },
    { name: 'Scheduled', value: vehicles.filter(v => v.maintenanceStatus === 'SCHEDULED_MAINTENANCE').length, color: '#f59e0b' }
  ].filter(d => d.value > 0);

  // 2. Fuel Type distributions
  const fuelCounts = vehicles.reduce((acc, v) => {
    acc[v.fuelType] = (acc[v.fuelType] || 0) + 1;
    return acc;
  }, {});
  const fuelData = Object.keys(fuelCounts).map(key => ({
    name: key,
    value: fuelCounts[key]
  }));
  const fuelColors = ['#0ea5e9', '#6366f1', '#a78bfa', '#f43f5e'];

  // 3. Route Scores Area Chart
  const routeScoreData = routes.map((r, idx) => ({
    name: r.routeName,
    score: r.routeScore || 100
  })).slice(-6); // show last 6 routes

  return (
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '24px', marginTop: '24px' }}>
      <div className="panel" style={{ minHeight: '320px', display: 'flex', flexDirection: 'column' }}>
        <h4 style={{ marginBottom: '16px', color: 'var(--text-main)' }}>Fleet Maintenance Status</h4>
        {maintData.length > 0 ? (
          <div style={{ flex: 1, minHeight: '220px' }}>
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={maintData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={60} label>
                  {maintData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip contentStyle={{ background: '#1e293b', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '8px', color: '#fff' }} />
                <Legend formatter={(value) => <span style={{ color: '#94a3b8', fontSize: '0.8rem' }}>{value}</span>} />
              </PieChart>
            </ResponsiveContainer>
          </div>
        ) : (
          <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#94a3b8' }}>No vehicle data available</div>
        )}
      </div>

      <div className="panel" style={{ minHeight: '320px', display: 'flex', flexDirection: 'column' }}>
        <h4 style={{ marginBottom: '16px', color: 'var(--text-main)' }}>Fuel Type Distribution</h4>
        {fuelData.length > 0 ? (
          <div style={{ flex: 1, minHeight: '220px' }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={fuelData}>
                <XAxis dataKey="name" stroke="#94a3b8" fontSize={11} tickLine={false} />
                <YAxis stroke="#94a3b8" fontSize={11} tickLine={false} />
                <Tooltip contentStyle={{ background: '#1e293b', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '8px', color: '#fff' }} />
                <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                  {fuelData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={fuelColors[index % fuelColors.length]} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        ) : (
          <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#94a3b8' }}>No fuel data available</div>
        )}
      </div>

      <div className="panel wide" style={{ minHeight: '320px', display: 'flex', flexDirection: 'column', gridColumn: '1 / -1' }}>
        <h4 style={{ marginBottom: '16px', color: 'var(--text-main)' }}>Route Optimization Score History (Last 6 Runs)</h4>
        {routeScoreData.length > 0 ? (
          <div style={{ flex: 1, minHeight: '220px' }}>
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={routeScoreData}>
                <defs>
                  <linearGradient id="scoreColor" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#0ea5e9" stopOpacity={0.4}/>
                    <stop offset="95%" stopColor="#0ea5e9" stopOpacity={0.0}/>
                  </linearGradient>
                </defs>
                <XAxis dataKey="name" stroke="#94a3b8" fontSize={11} tickLine={false} />
                <YAxis stroke="#94a3b8" fontSize={11} tickLine={false} domain={[0, 100]} />
                <Tooltip contentStyle={{ background: '#1e293b', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '8px', color: '#fff' }} />
                <Area type="monotone" dataKey="score" stroke="#0ea5e9" strokeWidth={3} fillOpacity={1} fill="url(#scoreColor)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        ) : (
          <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#94a3b8' }}>No optimized route score history available</div>
        )}
      </div>
    </div>
  );
}
