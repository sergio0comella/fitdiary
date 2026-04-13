import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { useWorkoutStore } from '../store/stores'

export default function AppShell() {
  const activeSession = useWorkoutStore(s => s.activeSession)
  const navigate = useNavigate()

  return (
    <div className="app-shell">
      <div className="page-content">
        <Outlet />
      </div>

      <nav className="bottom-nav">
        <NavLink to="/" end className={({ isActive }) => `nav-btn${isActive ? ' active' : ''}`}>
          <span className="nav-icon">🏠</span>
          <span>Home</span>
        </NavLink>
        <NavLink to="/plans" className={({ isActive }) => `nav-btn${isActive ? ' active' : ''}`}>
          <span className="nav-icon">📋</span>
          <span>Schede</span>
        </NavLink>
        <button
          className={`nav-btn${activeSession ? ' active' : ''}`}
          onClick={() => navigate('/log')}
          style={activeSession ? { color: 'var(--green)' } : {}}
        >
          <span className="nav-icon">{activeSession ? '⏱' : '▶️'}</span>
          <span>{activeSession ? 'Live' : 'Allena'}</span>
        </button>
        <NavLink to="/progress" className={({ isActive }) => `nav-btn${isActive ? ' active' : ''}`}>
          <span className="nav-icon">📈</span>
          <span>Progressi</span>
        </NavLink>
        <NavLink to="/profile" className={({ isActive }) => `nav-btn${isActive ? ' active' : ''}`}>
          <span className="nav-icon">👤</span>
          <span>Profilo</span>
        </NavLink>
      </nav>
    </div>
  )
}
