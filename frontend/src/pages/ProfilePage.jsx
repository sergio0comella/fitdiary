import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { userAPI, authAPI, statsAPI } from '../services/api'
import { useAuthStore } from '../store/stores'
import toast from 'react-hot-toast'

export default function ProfilePage() {
  const { user, setUser, logout } = useAuthStore()
  const [stats, setStats] = useState(null)
  const [editOpen, setEditOpen] = useState(false)
  const [form, setForm] = useState({})
  const navigate = useNavigate()

  useEffect(() => {
    statsAPI.getStats().then(r => setStats(r.data.data)).catch(() => {})
  }, [])

  const openEdit = () => {
    setForm({
      name: user?.name || '',
      age: user?.age || '',
      weightKg: user?.weightKg || '',
      heightCm: user?.heightCm || '',
      goal: user?.goal || 'Massa',
      level: user?.level || 'BEGINNER'
    })
    setEditOpen(true)
  }

  const saveProfile = async (e) => {
    e.preventDefault()
    try {
      const { data } = await userAPI.updateMe({
        ...form,
        age: form.age ? parseInt(form.age) : null,
        weightKg: form.weightKg ? parseFloat(form.weightKg) : null,
        heightCm: form.heightCm ? parseFloat(form.heightCm) : null,
      })
      setUser(data.data)
      setEditOpen(false)
      toast.success('Profilo aggiornato!')
    } catch { toast.error('Errore aggiornamento') }
  }

  const handleLogout = async () => {
    try { await authAPI.logout() } catch {}
    logout()
    navigate('/login')
  }

  const bmi = user?.weightKg && user?.heightCm
    ? (user.weightKg / Math.pow(user.heightCm / 100, 2)).toFixed(1)
    : '—'

  const fmtHours = s => s ? Math.round(s / 3600) : 0
  const fmtVol = v => v ? (v >= 1000 ? `${(v / 1000).toFixed(0)}k` : Math.round(v)) : 0

  const levelLabel = { BEGINNER: 'Principiante', INTERMEDIATE: 'Intermedio', ADVANCED: 'Avanzato' }

  return (
    <div>
      <div className="page-header">
        <span className="page-title">Profilo</span>
        <button className="btn btn-ghost btn-sm" onClick={openEdit}>Modifica</button>
      </div>

      {/* Avatar */}
      <div style={{ textAlign: 'center', padding: '20px 16px 4px' }}>
        <div style={{
          width: 72, height: 72, borderRadius: '50%', margin: '0 auto 12px',
          background: 'linear-gradient(135deg, var(--accent), #8b5cf6)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: 28, fontWeight: 700, color: '#fff'
        }}>
          {(user?.name || 'A')[0].toUpperCase()}
        </div>
        <div style={{ fontSize: 20, fontWeight: 700 }}>{user?.name}</div>
        <div style={{ color: 'var(--text2)', fontSize: 13, marginTop: 3 }}>
          {levelLabel[user?.level] || user?.level} · {user?.goal}
        </div>
      </div>

      {/* Body stats */}
      <div className="card" style={{ marginTop: 16 }}>
        <div className="card-title" style={{ marginBottom: 12 }}>Dati Fisici</div>
        {[
          ['Peso', user?.weightKg ? `${user.weightKg} kg` : '—'],
          ['Altezza', user?.heightCm ? `${user.heightCm} cm` : '—'],
          ['Età', user?.age ? `${user.age} anni` : '—'],
          ['BMI', bmi],
        ].map(([k, v]) => (
          <div key={k} style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid var(--border)' }}>
            <span style={{ color: 'var(--text2)' }}>{k}</span>
            <span style={{ fontWeight: 500 }}>{v}</span>
          </div>
        ))}
      </div>

      {/* Training stats */}
      {stats && (
        <>
          <div className="section-label">Statistiche Totali</div>
          <div className="stats-strip">
            <div className="stat-card">
              <div className="stat-num">{stats.totalSessions}</div>
              <div className="stat-label">Sessioni</div>
            </div>
            <div className="stat-card">
              <div className="stat-num">{fmtVol(stats.totalVolumeKg)}</div>
              <div className="stat-label">kg Totali</div>
            </div>
            <div className="stat-card">
              <div className="stat-num">{stats.currentStreak}</div>
              <div className="stat-label">Streak 🔥</div>
            </div>
          </div>
        </>
      )}

      {/* Actions */}
      <div style={{ padding: '0 16px', display: 'flex', flexDirection: 'column', gap: 10, marginBottom: 24 }}>
        <button className="btn btn-ghost" onClick={openEdit}>✏️ Modifica Profilo</button>
        <button className="btn btn-danger" onClick={handleLogout}>🚪 Logout</button>
      </div>

      {/* Edit modal */}
      {editOpen && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setEditOpen(false)}>
          <div className="modal">
            <div className="modal-handle" />
            <div className="modal-title">Modifica Profilo</div>
            <form onSubmit={saveProfile}>
              <div className="field"><label>Nome</label>
                <input value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} />
              </div>
              <div className="row2">
                <div className="field"><label>Peso (kg)</label>
                  <input type="number" value={form.weightKg} onChange={e => setForm({ ...form, weightKg: e.target.value })} />
                </div>
                <div className="field"><label>Altezza (cm)</label>
                  <input type="number" value={form.heightCm} onChange={e => setForm({ ...form, heightCm: e.target.value })} />
                </div>
              </div>
              <div className="row2">
                <div className="field"><label>Età</label>
                  <input type="number" value={form.age} onChange={e => setForm({ ...form, age: e.target.value })} />
                </div>
                <div className="field"><label>Livello</label>
                  <select value={form.level} onChange={e => setForm({ ...form, level: e.target.value })}>
                    <option value="BEGINNER">Principiante</option>
                    <option value="INTERMEDIATE">Intermedio</option>
                    <option value="ADVANCED">Avanzato</option>
                  </select>
                </div>
              </div>
              <div className="field"><label>Obiettivo</label>
                <select value={form.goal} onChange={e => setForm({ ...form, goal: e.target.value })}>
                  {['Massa','Forza','Definizione','Salute'].map(g => <option key={g}>{g}</option>)}
                </select>
              </div>
              <div style={{ display: 'flex', gap: 8 }}>
                <button type="submit" className="btn" style={{ flex: 1 }}>Salva</button>
                <button type="button" className="btn btn-ghost" onClick={() => setEditOpen(false)}>Annulla</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
