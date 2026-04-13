import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authAPI } from '../services/api'
import { useAuthStore } from '../store/stores'
import toast from 'react-hot-toast'

export default function RegisterPage() {
  const [form, setForm] = useState({
    email: '', password: '', name: '',
    age: '', weightKg: '', heightCm: '',
    goal: 'Massa', level: 'BEGINNER'
  })
  const [loading, setLoading] = useState(false)
  const setAuth = useAuthStore(s => s.setAuth)
  const navigate = useNavigate()

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const payload = {
        ...form,
        age: form.age ? parseInt(form.age) : null,
        weightKg: form.weightKg ? parseFloat(form.weightKg) : null,
        heightCm: form.heightCm ? parseFloat(form.heightCm) : null,
      }
      const { data } = await authAPI.register(payload)
      const { user, accessToken, refreshToken } = data.data
      setAuth(user, accessToken, refreshToken)
      toast.success('Benvenuto in FitDiary! 💪')
      navigate('/')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Errore durante la registrazione')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-logo">💪 FitDiary</div>
        <div className="auth-sub">Crea il tuo account</div>

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label>Nome *</label>
            <input required value={form.name} onChange={e => set('name', e.target.value)} placeholder="Mario Rossi" />
          </div>
          <div className="field">
            <label>Email *</label>
            <input type="email" required value={form.email} onChange={e => set('email', e.target.value)} placeholder="mario@example.com" />
          </div>
          <div className="field">
            <label>Password * (min 6 caratteri)</label>
            <input type="password" required minLength={6} value={form.password} onChange={e => set('password', e.target.value)} placeholder="••••••••" />
          </div>

          <div className="row2">
            <div className="field">
              <label>Peso (kg)</label>
              <input type="number" value={form.weightKg} onChange={e => set('weightKg', e.target.value)} placeholder="80" />
            </div>
            <div className="field">
              <label>Altezza (cm)</label>
              <input type="number" value={form.heightCm} onChange={e => set('heightCm', e.target.value)} placeholder="178" />
            </div>
          </div>

          <div className="row2">
            <div className="field">
              <label>Età</label>
              <input type="number" value={form.age} onChange={e => set('age', e.target.value)} placeholder="25" />
            </div>
            <div className="field">
              <label>Livello</label>
              <select value={form.level} onChange={e => set('level', e.target.value)}>
                <option value="BEGINNER">Principiante</option>
                <option value="INTERMEDIATE">Intermedio</option>
                <option value="ADVANCED">Avanzato</option>
              </select>
            </div>
          </div>

          <div className="field">
            <label>Obiettivo</label>
            <select value={form.goal} onChange={e => set('goal', e.target.value)}>
              <option>Massa</option>
              <option>Forza</option>
              <option>Definizione</option>
              <option>Salute</option>
            </select>
          </div>

          <button type="submit" className="btn btn-block" disabled={loading}>
            {loading ? <span className="spinner" /> : 'Crea Account'}
          </button>
        </form>

        <p style={{ textAlign: 'center', marginTop: 20, color: 'var(--text2)', fontSize: 13 }}>
          Hai già un account?{' '}
          <Link to="/login" style={{ color: 'var(--accent2)' }}>Accedi</Link>
        </p>
      </div>
    </div>
  )
}
