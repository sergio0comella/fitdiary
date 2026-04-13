import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { statsAPI, workoutsAPI } from '../services/api'
import { useAuthStore } from '../store/stores'
import { formatDistanceToNow } from 'date-fns'
import { it } from 'date-fns/locale'

export default function HomePage() {
  const user = useAuthStore(s => s.user)
  const [stats, setStats] = useState(null)
  const [sessions, setSessions] = useState([])
  const [insights, setInsights] = useState([])
  const navigate = useNavigate()

  useEffect(() => {
    statsAPI.getStats().then(r => setStats(r.data.data)).catch(() => {})
    workoutsAPI.getSessions(5).then(r => setSessions(r.data.data)).catch(() => {})
    statsAPI.getInsights().then(r => setInsights(r.data.data)).catch(() => {})
  }, [])

  const greetingText = () => {
    const h = new Date().getHours()
    if (h < 12) return 'Buongiorno'
    if (h < 18) return 'Buon pomeriggio'
    return 'Buonasera'
  }

  const fmtVol = v => v >= 1000 ? `${(v / 1000).toFixed(1)}k` : Math.round(v)
  const fmtDur = s => {
    if (!s) return '—'
    const m = Math.floor(s / 60)
    return m < 60 ? `${m}min` : `${Math.floor(m / 60)}h ${m % 60}min`
  }

  return (
    <div>
      {/* Greeting */}
      <div style={{ padding: '24px 16px 8px' }}>
        <div style={{ fontSize: 26, fontWeight: 700, letterSpacing: '-0.5px' }}>
          {greetingText()}, {user?.name?.split(' ')[0]} 👋
        </div>
        <div style={{ color: 'var(--text2)', fontSize: 13, marginTop: 3 }}>
          {new Date().toLocaleDateString('it-IT', { weekday: 'long', day: 'numeric', month: 'long' })}
        </div>
      </div>

      {/* Stats strip */}
      {stats && (
        <div className="stats-strip">
          <div className="stat-card">
            <div className="stat-num">{stats.sessionsThisWeek}</div>
            <div className="stat-label">Sessioni / 7gg</div>
          </div>
          <div className="stat-card">
            <div className="stat-num">{fmtVol(stats.volumeThisWeek || 0)}</div>
            <div className="stat-label">Volume kg</div>
          </div>
          <div className="stat-card">
            <div className="stat-num">{stats.currentStreak}</div>
            <div className="stat-label">Streak 🔥</div>
          </div>
        </div>
      )}

      {/* Insights */}
      {insights.map((ins, i) => (
        <div key={i} className="insight-card">
          <span className="insight-icon">
            {ins.type === 'IMPROVEMENT' ? '📈' :
             ins.type === 'VOLUME_DROP' ? '⚠️' :
             ins.type === 'DELOAD_SUGGESTION' ? '😴' :
             ins.type === 'PR' ? '🏆' : '💡'}
          </span>
          <span className="insight-text">{ins.message}</span>
        </div>
      ))}

      {/* Quick start */}
      <div style={{ padding: '4px 16px 16px' }}>
        <button className="btn btn-block btn-success" onClick={() => navigate('/log')}>
          ▶ Inizia Allenamento
        </button>
      </div>

      {/* Recent sessions */}
      <div className="section-label" style={{ marginTop: 4 }}>Allenamenti Recenti</div>
      {sessions.length === 0 ? (
        <div className="empty">
          <div className="empty-icon">📓</div>
          <div className="empty-text">Nessun allenamento registrato.<br />Inizia il tuo primo workout!</div>
        </div>
      ) : (
        sessions.map(s => (
          <div key={s.id} className="workout-item" onClick={() => navigate('/progress')}>
            <div className="workout-emoji">{getDayEmoji(s.dayName)}</div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div className="workout-name">{s.dayName || 'Sessione'}</div>
              <div className="workout-meta">
                {s.planName} · {fmtVol(s.totalVolumeKg || 0)} kg
              </div>
            </div>
            <div style={{ fontSize: 12, color: 'var(--text3)', textAlign: 'right' }}>
              <div>{formatDistanceToNow(new Date(s.startedAt), { addSuffix: true, locale: it })}</div>
              <div>{fmtDur(s.durationSeconds)}</div>
            </div>
          </div>
        ))
      )}
      <div style={{ height: 8 }} />
    </div>
  )
}

function getDayEmoji(day) {
  if (!day) return '💪'
  const d = day.toLowerCase()
  if (d.includes('push')) return '🫸'
  if (d.includes('pull')) return '🤲'
  if (d.includes('leg') || d.includes('gamb')) return '🦵'
  if (d.includes('upper') || d.includes('petto') || d.includes('schiena')) return '💪'
  if (d.includes('lower') || d.includes('forza')) return '🏋️'
  return '⚡'
}
